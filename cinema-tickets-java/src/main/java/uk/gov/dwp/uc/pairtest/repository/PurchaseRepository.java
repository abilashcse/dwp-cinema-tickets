package uk.gov.dwp.uc.pairtest.repository;

import uk.gov.dwp.uc.pairtest.domain.Purchase;

import java.util.List;

public interface PurchaseRepository {
    void save(Purchase purchase);

    List<Purchase> findAll();
}
