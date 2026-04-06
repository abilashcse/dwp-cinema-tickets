package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.AccountIdValidator;
import uk.gov.dwp.uc.pairtest.validation.BusinessRulesValidator;
import uk.gov.dwp.uc.pairtest.validation.PurchaseContext;
import uk.gov.dwp.uc.pairtest.validation.PurchaseRequest;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;
import uk.gov.dwp.uc.pairtest.validation.TicketTypeRequestsValidator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123L;

    @Test
    void rejectsNullAccountId() {
        var validator = new AccountIdValidator();
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))));
    }

    @Test
    void rejectsNonPositiveAccountId() {
        var validator = new AccountIdValidator();

        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))));
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))));
    }

    @Test
    void rejectsNullRequestsArray() {
        var validator = new TicketTypeRequestsValidator();

        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(VALID_ACCOUNT_ID, (TicketTypeRequest[]) null)));
    }

    @Test
    void rejectsEmptyRequestsArray() {
        var validator = new TicketTypeRequestsValidator();

        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(VALID_ACCOUNT_ID, new TicketTypeRequest[]{})));
    }

    @Test
    void rejectsRequestWithNullType() {
        assertThrows(IllegalArgumentException.class, () -> new TicketTypeRequest(null, 1));
    }

    @Test
    void rejectsRequestWithNonPositiveTicketCount() {
        assertThrows(IllegalArgumentException.class, () -> new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0));
        assertThrows(IllegalArgumentException.class, () -> new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1));
    }

    @Test
    void rejectsMoreThan25TicketsInTotal() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                26, 0, 0,
                26,
                0,
                0
        );
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseContext(VALID_ACCOUNT_ID, null, summary)));
    }

    @Test
    void rejectsChildPurchaseWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                0, 1, 0,
                1,
                0,
                0
        );
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseContext(VALID_ACCOUNT_ID, null, summary)));
    }

    @Test
    void rejectsInfantPurchaseWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                0, 0, 1,
                1,
                0,
                0
        );
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseContext(VALID_ACCOUNT_ID, null, summary)));
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

        verify(payment).makePayment(VALID_ACCOUNT_ID, 40);
        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
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

        verify(payment).makePayment(VALID_ACCOUNT_ID, 70);
        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 5);
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

        verify(payment).makePayment(VALID_ACCOUNT_ID, 40);
        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
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

        verify(payment).makePayment(VALID_ACCOUNT_ID, 30);
        verify(seats).reserveSeat(VALID_ACCOUNT_ID, 2);
        verifyNoMoreInteractions(payment, seats);
    }

    @Test
    void successfulPurchaseIsSavedToRepository() {
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

        verify(repo).save(any());
    }

    private static TicketServiceImpl serviceWithMocks(TicketPaymentService paymentService,
                                                     SeatReservationService seatReservationService) {
        return serviceWithMocks(paymentService, seatReservationService, mock(PurchaseRepository.class));
    }

    private static TicketServiceImpl serviceWithMocks(TicketPaymentService paymentService,
                                                     SeatReservationService seatReservationService,
                                                     PurchaseRepository purchaseRepository) {
        return new TicketServiceImpl(paymentService, seatReservationService, purchaseRepository);
    }
}
