package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@FunctionalInterface
public interface PurchaseValidator<T> {
    void validate(T target) throws InvalidPurchaseException;
}

