package uk.gov.dwp.uc.pairtest.validation;

public record PurchaseReceipt(
        String bookingId,
        PurchaseSummary summary
) {
}

