package uk.gov.dwp.uc.pairtest.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@Component
public final class BusinessRulesValidator implements PurchaseValidator<PurchaseSummary> {

    private final int maxTicketsPerPurchase;

    public BusinessRulesValidator(@Value("${purchase.max-tickets:25}") int maxTicketsPerPurchase) {
        this.maxTicketsPerPurchase = maxTicketsPerPurchase;
    }

    @Override
    public void validate(PurchaseSummary summary) throws InvalidPurchaseException {
        if (summary == null) {
            throw new InvalidPurchaseException("Purchase summary is required");
        }

        if (summary.totalTickets() == 0) {
            throw new InvalidPurchaseException("At least one ticket must be purchased");
        }

        if (summary.totalTickets() > maxTicketsPerPurchase) {
            throw new InvalidPurchaseException("Total tickets must not exceed " + maxTicketsPerPurchase);
        }

        if ((summary.children() > 0 || summary.infants() > 0) && summary.adults() == 0) {
            throw new InvalidPurchaseException("Child and infant tickets require at least one adult ticket");
        }

        if (summary.infants() > summary.adults()) {
            throw new InvalidPurchaseException("Number of infants cannot exceed number of adults");
        }
    }
}
