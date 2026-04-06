package uk.gov.dwp.uc.pairtest.integration;

import org.springframework.stereotype.Component;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;

@Component
public class ThirdPartySeatReservationService implements SeatReservationService {

    private final SeatReservationService delegate = new SeatReservationServiceImpl();

    @Override
    public void reserveSeat(long accountId, int totalSeatsToAllocate) {
        delegate.reserveSeat(accountId, totalSeatsToAllocate);
    }
}

