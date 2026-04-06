package uk.gov.dwp.uc.pairtest.api;

import java.util.List;

public record TicketRatesResponseDto(
        List<TicketRate> rates,
        int maxTicketsPerPurchase
) {
    public record TicketRate(String type, int price, boolean requiresSeat) {
    }
}
