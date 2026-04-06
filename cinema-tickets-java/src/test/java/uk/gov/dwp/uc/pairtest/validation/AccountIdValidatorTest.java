package uk.gov.dwp.uc.pairtest.validation;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountIdValidatorTest {

    private final AccountIdValidator validator = new AccountIdValidator();

    private static PurchaseRequest requestWithAccountId(Long accountId) {
        return new PurchaseRequest(accountId, new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
        });
    }

    @Test
    void rejectsNullRequest() {
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(null));
    }

    @Test
    void rejectsNullAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(requestWithAccountId(null)));
    }

    @Test
    void rejectsZeroAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(requestWithAccountId(0L)));
    }

    @Test
    void rejectsNegativeAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(requestWithAccountId(-1L)));
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(requestWithAccountId(-999L)));
    }

    @Test
    void acceptsPositiveAccountId() {
        assertDoesNotThrow(() -> validator.validate(requestWithAccountId(1L)));
        assertDoesNotThrow(() -> validator.validate(requestWithAccountId(999L)));
    }
}
