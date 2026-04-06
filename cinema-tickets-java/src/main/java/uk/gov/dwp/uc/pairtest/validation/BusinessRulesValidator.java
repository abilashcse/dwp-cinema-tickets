package uk.gov.dwp.uc.pairtest.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@Component
public final class BusinessRulesValidator implements PurchaseValidator<PurchaseContext> {

    private final int maxTicketsPerPurchase;

    public BusinessRulesValidator(@Value("${purchase.max-tickets:25}") int maxTicketsPerPurchase) {
        this.maxTicketsPerPurchase = maxTicketsPerPurchase;
    }

    @Override
    public void validate(PurchaseContext ctx) throws InvalidPurchaseException {
        if (ctx == null || ctx.summary() == null) {
            throw new InvalidPurchaseException();
        }

        if (ctx.summary().totalTickets() > maxTicketsPerPurchase) {
            throw new InvalidPurchaseException();
        }

        if ((ctx.summary().children() > 0 || ctx.summary().infants() > 0) && ctx.summary().adults() == 0) {
            throw new InvalidPurchaseException();
        }
    }
}

