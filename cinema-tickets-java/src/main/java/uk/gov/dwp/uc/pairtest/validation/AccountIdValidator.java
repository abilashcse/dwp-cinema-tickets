package uk.gov.dwp.uc.pairtest.validation;

import org.springframework.stereotype.Component;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@Component
public final class AccountIdValidator implements PurchaseValidator<PurchaseRequest> {
    @Override
    public void validate(PurchaseRequest request) throws InvalidPurchaseException {
        if (request == null || request.accountId() == null || request.accountId() <= 0) {
            throw new InvalidPurchaseException("Account ID must be greater than 0");
        }
    }
}
