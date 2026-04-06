package uk.gov.dwp.uc.pairtest.data;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Primary
public class InMemoryPurchaseRepository implements PurchaseRepository {
    private final List<Purchase> purchases = new CopyOnWriteArrayList<>();

    @Override
    public void save(Purchase purchase) {
        purchases.add(purchase);
    }

    @Override
    public List<Purchase> findAll() {
        return List.copyOf(purchases);
    }
}
