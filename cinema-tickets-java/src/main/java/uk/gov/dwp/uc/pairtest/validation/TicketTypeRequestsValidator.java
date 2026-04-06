package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public final class TicketTypeRequestsValidator implements PurchaseValidator<PurchaseRequest> {
    @Override
    public void validate(PurchaseRequest request) throws InvalidPurchaseException {
        if (request == null || request.ticketTypeRequests() == null || request.ticketTypeRequests().length == 0) {
            throw new InvalidPurchaseException();
        }

        for (TicketTypeRequest req : request.ticketTypeRequests()) {
            if (req == null || req.getTicketType() == null || req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }
        }
    }
}

