package uk.gov.dwp.uc.pairtest.validation;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessRulesValidatorTest {

    @Test
    void rejectsWhenSummaryIsNull() {
        var validator = new BusinessRulesValidator(25);
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(null));
    }

    @Test
    void rejectsWhenTotalTicketsIsZero() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(0, 0, 0, 0, 0, 0);
        assertThrows(InvalidPurchaseException.class, () -> validator.validate(summary));
    }

    @Test
    void rejectsWhenTotalTicketsExceedsMax() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(26, 0, 0, 26, 520, 26);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(summary));
    }

    @Test
    void allowsExactlyMaxTickets() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(25, 0, 0, 25, 500, 25);

        assertDoesNotThrow(() -> validator.validate(summary));
    }

    @Test
    void rejectsChildWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(0, 1, 0, 1, 10, 1);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(summary));
    }

    @Test
    void rejectsInfantWithoutAdult() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(0, 0, 1, 1, 0, 0);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(summary));
    }

    @Test
    void rejectsMoreInfantsThanAdults() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(1, 0, 2, 3, 20, 1);

        assertThrows(InvalidPurchaseException.class, () -> validator.validate(summary));
    }

    @Test
    void allowsEqualInfantsAndAdults() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(2, 0, 2, 4, 40, 2);

        assertDoesNotThrow(() -> validator.validate(summary));
    }

    @Test
    void allowsValidPurchase() {
        var validator = new BusinessRulesValidator(25);
        var summary = new PurchaseSummary(1, 1, 1, 3, 30, 2);

        assertDoesNotThrow(() -> validator.validate(summary));
    }
}
