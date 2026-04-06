package uk.gov.dwp.uc.pairtest.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.uc.pairtest.CinemaTicketService;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final CinemaTicketService ticketService;
    private final PurchaseRepository purchaseRepository;
    private final int maxTicketsPerPurchase;

    public PurchaseController(CinemaTicketService ticketService,
                              PurchaseRepository purchaseRepository,
                              @Value("${purchase.max-tickets:25}") int maxTicketsPerPurchase) {
        this.ticketService = ticketService;
        this.purchaseRepository = purchaseRepository;
        this.maxTicketsPerPurchase = maxTicketsPerPurchase;
    }

    @GetMapping
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        return ResponseEntity.ok(purchaseRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<PurchaseResponseDto> purchase(@Valid @RequestBody PurchaseRequestDto body) {
        validateBusinessRules(body);

        var requests = toTicketTypeRequests(body);
        PurchaseReceipt receipt = ticketService.purchaseTicketsWithReceipt(body.accountId(), requests);
        PurchaseSummary summary = receipt.summary();

        var response = new PurchaseResponseDto(
                receipt.bookingId(),
                body.accountId(),
                summary.adults(),
                summary.children(),
                summary.infants(),
                summary.totalTickets(),
                summary.totalAmountToPay(),
                summary.totalSeatsToAllocate(),
                "Purchase confirmed"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validateBusinessRules(PurchaseRequestDto body) {
        List<String> violations = new ArrayList<>();

        long adults = body.adultCount();
        long children = body.childCount();
        long infants = body.infantCount();

        long totalTickets = adults + children + infants;
        if (totalTickets == 0) {
            violations.add("At least one ticket must be purchased");
        }
        if (totalTickets > maxTicketsPerPurchase) {
            violations.add("Total tickets must be <= " + maxTicketsPerPurchase);
        }
        if ((children > 0 || infants > 0) && adults == 0) {
            violations.add("Child and/or infant tickets require at least 1 adult ticket");
        }
        if (infants > adults) {
            violations.add("Number of infants cannot exceed number of adults");
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
