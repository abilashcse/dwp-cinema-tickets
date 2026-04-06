package uk.gov.dwp.uc.pairtest.validation;

public record PurchaseSummary(
        int adults,
        int children,
        int infants,
        int totalTickets,
        int totalAmountToPay,
        int totalSeatsToAllocate
) {
}

