package uk.gov.dwp.uc.pairtest.domain;

import java.time.Instant;

public record Purchase(
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
            long accountId,
            int adults,
            int children,
            int infants,
            int totalTickets,
            int totalAmountToPay,
            int totalSeatsToAllocate
    ) {
        return new Purchase(
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

