package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public record PurchaseRequest(Long accountId, TicketTypeRequest[] ticketTypeRequests) {
    public PurchaseRequest {
        ticketTypeRequests = ticketTypeRequests == null ? null : ticketTypeRequests.clone();
    }
}
