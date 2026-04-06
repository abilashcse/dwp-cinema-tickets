package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public record PurchaseContext(Long accountId, TicketTypeRequest[] ticketTypeRequests, PurchaseSummary summary) {
}

