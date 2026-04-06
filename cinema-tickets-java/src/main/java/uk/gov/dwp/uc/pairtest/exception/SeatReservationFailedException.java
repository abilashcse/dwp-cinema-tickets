package uk.gov.dwp.uc.pairtest.exception;

public class SeatReservationFailedException extends RuntimeException {
    public SeatReservationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

