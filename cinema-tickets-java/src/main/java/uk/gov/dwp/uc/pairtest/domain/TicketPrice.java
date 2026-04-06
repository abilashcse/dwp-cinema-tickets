package uk.gov.dwp.uc.pairtest.domain;

/**
 * Kept for backwards compatibility with earlier iterations of the kata.
 *
 * Pricing is now configured via application properties (`ticket-pricing.*`) and is injected where needed.
 */
@Deprecated(forRemoval = false)
public enum TicketPrice {
    ADULT,
    CHILD,
    INFANT;

    public static TicketPrice from(TicketTypeRequest.Type type) {
        return switch (type) {
            case ADULT -> ADULT;
            case CHILD -> CHILD;
            case INFANT -> INFANT;
        };
    }
}

