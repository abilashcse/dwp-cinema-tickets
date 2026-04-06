package uk.gov.dwp.uc.pairtest.validation;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessRulesValidatorTest {

    private static final long VALID_ACCOUNT_ID = 123L;

    @Test
    void rejectsWhenContextIsNull() {
        var validator = new BusinessRulesValidator(25);
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(null));
    }

    @Test
    void rejectsWhenSummaryIsNull() {
        var validator = new BusinessRulesValidator(25);
        var ctx = new PurchaseContext(VALID_ACCOUNT_ID, null, null);
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(ctx));
    }

    @Test
    void rejectsWhenTotalTicketsExceedsMax() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                26, 0, 0,
                26,
                0,
                0
        );
        var ctx = new PurchaseContext(VALID_ACCOUNT_ID, null, summary);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(ctx));
    }

    @Test
    void rejectsChildWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                0, 1, 0,
                1,
                0,
                0
        );
        var ctx = new PurchaseContext(VALID_ACCOUNT_ID, null, summary);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(ctx));
    }

    @Test
    void rejectsInfantWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                0, 0, 1,
                1,
                0,
                0
        );
        var ctx = new PurchaseContext(VALID_ACCOUNT_ID, null, summary);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(ctx));
    }

    @Test
    void allowsValidPurchase() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(
                1, 1, 1,
                3,
                0,
                0
        );
        var ctx = new PurchaseContext(VALID_ACCOUNT_ID, null, summary);

        assertDoesNotThrow(() -> validator.validate(ctx));
    }
}

