package uk.gov.dwp.uc.pairtest.repository;

import uk.gov.dwp.uc.pairtest.domain.Purchase;

public interface PurchaseRepository {
    void save(Purchase purchase);
}

