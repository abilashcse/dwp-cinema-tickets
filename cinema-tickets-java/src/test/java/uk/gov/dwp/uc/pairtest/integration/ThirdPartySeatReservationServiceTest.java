package uk.gov.dwp.uc.pairtest.integration;

import org.junit.jupiter.api.Test;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.exception.SeatReservationFailedException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThirdPartySeatReservationServiceTest {

    @Test
    void wrapsDelegateExceptions() {
        SeatReservationService throwingDelegate = new SeatReservationService() {
            @Override
            public void reserveSeat(long accountId, int totalSeatsToAllocate) {
                throw new RuntimeException("boom");
            }
        };
        ThirdPartySeatReservationService service = new ThirdPartySeatReservationService(throwingDelegate);

        assertThrows(SeatReservationFailedException.class, () -> service.reserveSeat(1L, 2));
    }
}

