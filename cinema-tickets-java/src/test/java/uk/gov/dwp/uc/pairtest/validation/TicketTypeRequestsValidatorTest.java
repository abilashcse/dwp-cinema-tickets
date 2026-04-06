package uk.gov.dwp.uc.pairtest.validation;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketTypeRequestsValidatorTest {

    private static final long VALID_ACCOUNT_ID = 123L;
    private final TicketTypeRequestsValidator validator = new TicketTypeRequestsValidator();

    @Test
    void rejectsNullRequest() {
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(null));
    }

    @Test
    void rejectsNullTicketTypeRequestsArray() {
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(VALID_ACCOUNT_ID, (TicketTypeRequest[]) null)));
    }

    @Test
    void rejectsEmptyTicketTypeRequestsArray() {
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(VALID_ACCOUNT_ID, new TicketTypeRequest[]{})));
    }

    @Test
    void rejectsArrayContainingNullElement() {
        assertThrows(InvalidPurchaseException.class, () ->
                validator.validate(new PurchaseRequest(VALID_ACCOUNT_ID, new TicketTypeRequest[]{null})));
    }

    @Test
    void acceptsValidSingleRequest() {
        var request = new PurchaseRequest(VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1));
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void acceptsValidMultipleRequests() {
        var request = new PurchaseRequest(VALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
        assertDoesNotThrow(() -> validator.validate(request));
    }
}
