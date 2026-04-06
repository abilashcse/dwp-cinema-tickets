package uk.gov.dwp.uc.pairtest.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private final TicketService ticketService;

    public PurchaseController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponseDto> purchase(@Valid @RequestBody PurchaseRequestDto body) {
        validateBusinessRules(body);

        var requests = toTicketTypeRequests(body);
        ticketService.purchaseTickets(body.accountId(), requests);

        int adults = body.adultCount();
        int children = body.childCount();
        int infants = body.infantCount();
        int totalTickets = adults + children + infants;
        int totalAmountToPay = (adults * TicketPrice.ADULT.price()) + (children * TicketPrice.CHILD.price());
        int totalSeatsToAllocate = adults + children;

        var response = new PurchaseResponseDto(
                body.accountId(),
                adults,
                children,
                infants,
                totalTickets,
                totalAmountToPay,
                totalSeatsToAllocate
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private static void validateBusinessRules(PurchaseRequestDto body) {
        List<String> violations = new ArrayList<>();

        int adults = body.adultCount();
        int children = body.childCount();
        int infants = body.infantCount();

        int totalTickets = adults + children + infants;
        if (totalTickets == 0) {
            violations.add("At least one ticket must be purchased");
        }
        if (totalTickets > MAX_TICKETS_PER_PURCHASE) {
            violations.add("Total tickets must be <= " + MAX_TICKETS_PER_PURCHASE);
        }
        if ((children > 0 || infants > 0) && adults == 0) {
            violations.add("Child and/or infant tickets require at least 1 adult ticket");
        }

        if (!violations.isEmpty()) {
            throw new ApiValidationException(violations);
        }
    }

    private static TicketTypeRequest[] toTicketTypeRequests(PurchaseRequestDto body) {
        List<TicketTypeRequest> requests = new ArrayList<>(3);

        if (body.adultCount() > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, body.adultCount()));
        }
        if (body.childCount() > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, body.childCount()));
        }
        if (body.infantCount() > 0) {
            requests.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, body.infantCount()));
        }

        return requests.toArray(TicketTypeRequest[]::new);
    }
}

