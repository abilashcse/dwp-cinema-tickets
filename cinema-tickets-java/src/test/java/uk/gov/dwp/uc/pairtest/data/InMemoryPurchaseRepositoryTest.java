package uk.gov.dwp.uc.pairtest.data;

import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.Purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPurchaseRepositoryTest {

    private final InMemoryPurchaseRepository repo = new InMemoryPurchaseRepository();

    @Test
    void findAllReturnsEmptyListInitially() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void savePersistsPurchase() {
        Purchase purchase = Purchase.of(1L, 2, 1, 0, 3, 65, 3);
        repo.save(purchase);

        assertEquals(1, repo.findAll().size());
        assertEquals(purchase, repo.findAll().get(0));
    }

    @Test
    void saveMultiplePurchases() {
        Purchase first = Purchase.of(1L, 1, 0, 0, 1, 25, 1);
        Purchase second = Purchase.of(2L, 2, 1, 1, 4, 65, 3);

        repo.save(first);
        repo.save(second);

        assertEquals(2, repo.findAll().size());
        assertEquals(first, repo.findAll().get(0));
        assertEquals(second, repo.findAll().get(1));
    }

    @Test
    void findAllReturnsImmutableSnapshot() {
        repo.save(Purchase.of(1L, 1, 0, 0, 1, 25, 1));

        var snapshot = repo.findAll();
        repo.save(Purchase.of(2L, 1, 0, 0, 1, 25, 1));

        assertEquals(1, snapshot.size(), "Snapshot should not reflect later additions");
        assertEquals(2, repo.findAll().size());
    }
}
