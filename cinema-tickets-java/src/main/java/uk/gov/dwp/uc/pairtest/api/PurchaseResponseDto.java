package uk.gov.dwp.uc.pairtest.api;

public record PurchaseResponseDto(
        String bookingId,
        Long accountId,
        int adults,
        int children,
        int infants,
        int totalTickets,
        int totalAmountToPay,
        int totalSeatsToAllocate,
        String message
) {
}

