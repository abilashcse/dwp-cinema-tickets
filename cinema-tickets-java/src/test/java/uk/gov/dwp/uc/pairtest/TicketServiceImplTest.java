package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
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
        verify(payment).makePayment(VALID_ACCOUNT_ID, 40);
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
        verify(payment).makePayment(VALID_ACCOUNT_ID, 70);
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
        verify(payment).makePayment(VALID_ACCOUNT_ID, 40);
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
        verify(payment).makePayment(VALID_ACCOUNT_ID, 30);
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
        assertEquals(30, saved.totalAmountToPay());
        assertEquals(2, saved.totalSeatsToAllocate());
    }

    @Test
    void returnedSummaryContainsCorrectValues() {
        var service = serviceWithMocks(mock(TicketPaymentService.class), mock(SeatReservationService.class));

        PurchaseSummary summary = service.purchaseTickets(
                VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        assertEquals(2, summary.adults());
        assertEquals(1, summary.children());
        assertEquals(1, summary.infants());
        assertEquals(4, summary.totalTickets());
        assertEquals(50, summary.totalAmountToPay());
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
                new AccountIdValidator(),
                new TicketTypeRequestsValidator(),
                new BusinessRulesValidator(25)
        );
    }
}
