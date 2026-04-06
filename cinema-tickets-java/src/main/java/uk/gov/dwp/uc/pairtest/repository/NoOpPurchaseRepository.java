package uk.gov.dwp.uc.pairtest.repository;

import uk.gov.dwp.uc.pairtest.domain.Purchase;

import java.util.List;

public final class NoOpPurchaseRepository implements PurchaseRepository {
    @Override
    public void save(Purchase purchase) {
    }

    @Override
    public List<Purchase> findAll() {
        return List.of();
    }
}
