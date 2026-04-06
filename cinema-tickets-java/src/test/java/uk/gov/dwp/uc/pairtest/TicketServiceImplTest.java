package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.booking.BookingIdGenerator;
import uk.gov.dwp.uc.pairtest.config.TicketPricingProperties;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.PaymentFailedException;
import uk.gov.dwp.uc.pairtest.exception.SeatReservationFailedException;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.AccountIdValidator;
import uk.gov.dwp.uc.pairtest.validation.BusinessRulesValidator;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;
import uk.gov.dwp.uc.pairtest.validation.TicketTypeRequestsValidator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123L;
    private static final TicketPricingProperties PRICING = loadPricingFromProperties();

    private static TicketPricingProperties loadPricingFromProperties() {
        try (var in = TicketServiceImplTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            var props = new java.util.Properties();
            if (in != null) {
                props.load(in);
            }
            int adult = Integer.parseInt(props.getProperty("ticket-pricing.adult", "25"));
            int child = Integer.parseInt(props.getProperty("ticket-pricing.child", "15"));
            int infant = Integer.parseInt(props.getProperty("ticket-pricing.infant", "0"));
            return new TicketPricingProperties(adult, child, infant);
        } catch (Exception e) {
            return new TicketPricingProperties(25, 15, 0);
        }
    }

    @Test
    void adultsOnlyChargesAndReservesSeats() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)
        );

        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
        verify(payment).makePayment(VALID_ACCOUNT_ID, 50);
        verifyNoMoreInteractions(payment, seats);
    }

    @Test
    void adultsAndChildrenChargesAndReservesSeats() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3)
        );

        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 5);
        verify(payment).makePayment(VALID_ACCOUNT_ID, 95);
        verifyNoMoreInteractions(payment, seats);
    }

    @Test
    void adultsAndInfantsInfantsDoNotAffectPaymentOrSeats() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
        verify(payment).makePayment(VALID_ACCOUNT_ID, 50);
        verifyNoMoreInteractions(payment, seats);
    }

    @Test
    void adultsChildrenAndInfantsSumsAreCorrect() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
        verify(payment).makePayment(VALID_ACCOUNT_ID, 40);
        verifyNoMoreInteractions(payment, seats);
    }

    @Test
    void successfulPurchaseIsSavedWithCorrectValues() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var repo = mock(PurchaseRepository.class);
        var service = serviceWithMocks(payment, seats, repo);

        service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        var captor = ArgumentCaptor.forClass(Purchase.class);
        verify(repo).save(captor.capture());

        Purchase saved = captor.getValue();
        assertEquals(VALID_ACCOUNT_ID, saved.accountId());
        assertEquals(1, saved.adults());
        assertEquals(1, saved.children());
        assertEquals(1, saved.infants());
        assertEquals(3, saved.totalTickets());
        assertEquals(40, saved.totalAmountToPay());
        assertEquals(2, saved.totalSeatsToAllocate());
    }

    @Test
    void returnedSummaryContainsCorrectValues() {
        var service = serviceWithMocks(mock(TicketPaymentService.class), mock(SeatReservationService.class));

        var receipt = service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );
        PurchaseSummary summary = receipt.summary();

        assertEquals(2, summary.adults());
        assertEquals(1, summary.children());
        assertEquals(1, summary.infants());
        assertEquals(4, summary.totalTickets());
        assertEquals(65, summary.totalAmountToPay());
        assertEquals(3, summary.totalSeatsToAllocate());
    }

    @Test
    void exactly25TicketsIsAllowed() {
        var service = serviceWithMocks(mock(TicketPaymentService.class), mock(SeatReservationService.class));

        assertDoesNotThrow(() -> service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25)
        ));
    }

    @Test
    void seatReservationFailureDoesNotChargePayment() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        doThrow(new SeatReservationFailedException("fail", new RuntimeException()))
                .when(seats).reserveSeat(anyLong(), anyInt());

        var service = serviceWithMocks(payment, seats);

        assertThrows(SeatReservationFailedException.class, () ->
                service.purchaseTickets(
                        VALID_ACCOUNT_ID,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                )
        );

        verify(payment, never()).makePayment(anyLong(), anyInt());
    }

    @Test
    void paymentFailureDoesNotSavePurchase() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var repo = mock(PurchaseRepository.class);
        doThrow(new PaymentFailedException("fail", new RuntimeException()))
                .when(payment).makePayment(anyLong(), anyInt());

        var service = serviceWithMocks(payment, seats, repo);

        assertThrows(PaymentFailedException.class, () ->
                service.purchaseTickets(
                        VALID_ACCOUNT_ID,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                )
        );

        verify(repo, never()).save(any());
    }

    @Test
    void repositoryFailurePropagatesAfterExternalCalls() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var repo = mock(PurchaseRepository.class);
        doThrow(new RuntimeException("db down"))
                .when(repo).save(any());

        var service = serviceWithMocks(payment, seats, repo);

        assertThrows(RuntimeException.class, () ->
                service.purchaseTickets(
                        VALID_ACCOUNT_ID,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                )
        );

        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 1);
        verify(payment).makePayment(VALID_ACCOUNT_ID, 25);
    }

    private static TicketServiceImpl serviceWithMocks(TicketPaymentService paymentService,
                                                     SeatReservationService seatReservationService) {
        return serviceWithMocks(paymentService, seatReservationService, mock(PurchaseRepository.class));
    }

    private static TicketServiceImpl serviceWithMocks(TicketPaymentService paymentService,
                                                     SeatReservationService seatReservationService,
                                                     PurchaseRepository purchaseRepository) {
        return new TicketServiceImpl(
                paymentService,
                seatReservationService,
                purchaseRepository,
                PRICING,
                new BookingIdGenerator(),
                new AccountIdValidator(),
                new TicketTypeRequestsValidator(),
                new BusinessRulesValidator(25)
        );
    }
}
