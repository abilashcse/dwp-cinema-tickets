package uk.gov.dwp.uc.pairtest.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

