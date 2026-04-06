package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public final class AccountIdValidator implements PurchaseValidator<PurchaseRequest> {
    @Override
    public void validate(PurchaseRequest request) throws InvalidPurchaseException {
        if (request == null || request.accountId() == null || request.accountId() <= 0) {
            throw new InvalidPurchaseException();
        }
    }
}

