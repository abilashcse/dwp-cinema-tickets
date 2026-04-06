package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private static final long VALID_ACCOUNT_ID = 123L;

    @Test
    void rejectsNullAccountId() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsNonPositiveAccountId() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsNullRequestsArray() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, (TicketTypeRequest[]) null));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsEmptyRequestsArray() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest[]{}));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsRequestWithNullType() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(null, 1)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsRequestWithNonPositiveTicketCount() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)));
        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsMoreThan25TicketsInTotal() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsChildPurchaseWithoutAdult() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)));

        verifyNoInteractions(payment, seats);
    }

    @Test
    void rejectsInfantPurchaseWithoutAdult() {
        var payment = mock(TicketPaymentService.class);
        var seats = mock(SeatReservationService.class);
        var service = serviceWithMocks(payment, seats);

        assertThrows(InvalidPurchaseException.class,
                () -> service.purchaseTickets(VALID_ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));

        verifyNoInteractions(payment, seats);
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
        var service = serviceWithMocks(payment, seats);
        inject(service, "purchaseRepository", repo);

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
        var service = new TicketServiceImpl();
        inject(service, "ticketPaymentService", paymentService);
        inject(service, "seatReservationService", seatReservationService);
        return service;
    }

    private static void inject(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            throw new AssertionError("Expected field '" + fieldName + "' on " + target.getClass().getName()
                    + " for test injection (add it and keep it injectable for tests).");
        }
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to inject field '" + fieldName + "' on " + target.getClass().getName(), e);
        }
    }

    private static Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
