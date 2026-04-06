package uk.gov.dwp.uc.pairtest.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ticket-rates")
public class TicketRateController {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    @GetMapping
    public ResponseEntity<TicketRatesResponseDto> getRates() {
        List<TicketRatesResponseDto.TicketRate> rates = Arrays.stream(TicketTypeRequest.Type.values())
                .map(type -> {
                    TicketPrice price = TicketPrice.from(type);
                    boolean requiresSeat = type != TicketTypeRequest.Type.INFANT;
                    return new TicketRatesResponseDto.TicketRate(
                            type.name(), price.price(), requiresSeat
                    );
                })
                .toList();

        return ResponseEntity.ok(new TicketRatesResponseDto(rates, MAX_TICKETS_PER_PURCHASE));
    }
}
