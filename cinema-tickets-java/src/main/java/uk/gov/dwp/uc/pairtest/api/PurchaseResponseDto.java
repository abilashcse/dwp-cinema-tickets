package uk.gov.dwp.uc.pairtest.api;

public record PurchaseResponseDto(
        Long accountId,
        int adults,
        int children,
        int infants,
        int totalTickets,
        int totalAmountToPay,
        int totalSeatsToAllocate
) {
}

