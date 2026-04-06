package uk.gov.dwp.uc.pairtest.domain;

import java.time.Instant;

public record Purchase(
        String bookingId,
        long accountId,
        int adults,
        int children,
        int infants,
        int totalTickets,
        int totalAmountToPay,
        int totalSeatsToAllocate,
        Instant createdAt
) {
    public static Purchase of(
            String bookingId,
            long accountId,
            int adults,
            int children,
            int infants,
            int totalTickets,
            int totalAmountToPay,
            int totalSeatsToAllocate
    ) {
        return new Purchase(
                bookingId,
                accountId,
                adults,
                children,
                infants,
                totalTickets,
                totalAmountToPay,
                totalSeatsToAllocate,
                Instant.now()
        );
    }
}

