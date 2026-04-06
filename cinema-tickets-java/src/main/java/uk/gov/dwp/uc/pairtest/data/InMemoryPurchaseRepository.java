package uk.gov.dwp.uc.pairtest.data;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Primary
public class InMemoryPurchaseRepository implements PurchaseRepository {
    private final List<Purchase> purchases = new ArrayList<>();

    @Override
    public void save(Purchase purchase) {
        purchases.add(purchase);
    }

    public List<Purchase> findAll() {
        return Collections.unmodifiableList(purchases);
    }
}

