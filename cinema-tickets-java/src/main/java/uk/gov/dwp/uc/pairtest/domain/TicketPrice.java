package uk.gov.dwp.uc.pairtest.domain;

public enum TicketPrice {
    ADULT(20),
    CHILD(10),
    INFANT(0);

    private final int price;

    TicketPrice(int price) {
        this.price = price;
    }

    public int price() {
        return price;
    }

    public static TicketPrice from(TicketTypeRequest.Type type) {
        return switch (type) {
            case ADULT -> ADULT;
            case CHILD -> CHILD;
            case INFANT -> INFANT;
        };
    }
}

