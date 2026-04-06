package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public final class BusinessRulesValidator implements PurchaseValidator<PurchaseContext> {

    private final int maxTicketsPerPurchase;

    public BusinessRulesValidator(int maxTicketsPerPurchase) {
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

