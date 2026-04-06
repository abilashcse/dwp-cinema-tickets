package uk.gov.dwp.uc.pairtest.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.exception.SeatReservationFailedException;

@Component
public class ThirdPartySeatReservationService implements SeatReservationService {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartySeatReservationService.class);

    private final SeatReservationService delegate;

    public ThirdPartySeatReservationService() {
        this(new SeatReservationServiceImpl());
    }

    ThirdPartySeatReservationService(SeatReservationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void reserveSeat(long accountId, int totalSeatsToAllocate) {
        try {
            delegate.reserveSeat(accountId, totalSeatsToAllocate);
        } catch (Exception e) {
            log.error("Seat reservation failed (accountId={}, seats={})", accountId, totalSeatsToAllocate, e);
            throw new SeatReservationFailedException("Seat reservation failed", e);
        }
    }
}

