package uk.gov.dwp.uc.pairtest.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.uc.pairtest.config.TicketPricingProperties;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ticket-rates")
public class TicketRateController {

    private final int maxTicketsPerPurchase;
    private final TicketPricingProperties ticketPricing;

    public TicketRateController(@Value("${purchase.max-tickets:25}") int maxTicketsPerPurchase,
                                TicketPricingProperties ticketPricing) {
        this.maxTicketsPerPurchase = maxTicketsPerPurchase;
        this.ticketPricing = ticketPricing;
    }

    @GetMapping
    public ResponseEntity<TicketRatesResponseDto> getRates() {
        List<TicketRatesResponseDto.TicketRate> rates = Arrays.stream(TicketTypeRequest.Type.values())
                .map(type -> {
                    boolean requiresSeat = type != TicketTypeRequest.Type.INFANT;
                    int price = switch (type) {
                        case ADULT -> ticketPricing.adult();
                        case CHILD -> ticketPricing.child();
                        case INFANT -> ticketPricing.infant();
                    };
                    return new TicketRatesResponseDto.TicketRate(
                            type.name(), price, requiresSeat
                    );
                })
                .toList();

        return ResponseEntity.ok(new TicketRatesResponseDto(rates, maxTicketsPerPurchase));
    }
}
