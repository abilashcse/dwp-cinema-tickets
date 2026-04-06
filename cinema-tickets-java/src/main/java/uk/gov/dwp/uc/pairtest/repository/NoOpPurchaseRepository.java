package uk.gov.dwp.uc.pairtest.repository;

import uk.gov.dwp.uc.pairtest.domain.Purchase;

public final class NoOpPurchaseRepository implements PurchaseRepository {
    @Override
    public void save(Purchase purchase) {
        // intentionally no-op
    }
}

