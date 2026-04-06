package uk.gov.dwp.uc.pairtest.data;

import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InMemoryPurchaseRepository implements PurchaseRepository {
    private final List<Purchase> purchases = new ArrayList<>();

    @Override
    public void save(Purchase purchase) {
        purchases.add(purchase);
    }

    public List<Purchase> findAll() {
        return Collections.unmodifiableList(purchases);
    }
}

