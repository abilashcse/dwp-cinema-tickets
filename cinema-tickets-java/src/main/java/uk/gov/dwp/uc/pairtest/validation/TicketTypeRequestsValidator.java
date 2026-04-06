package uk.gov.dwp.uc.pairtest.validation;

import org.springframework.stereotype.Component;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@Component
public final class TicketTypeRequestsValidator implements PurchaseValidator<PurchaseRequest> {
    @Override
    public void validate(PurchaseRequest request) throws InvalidPurchaseException {
        if (request == null || request.ticketTypeRequests() == null || request.ticketTypeRequests().length == 0) {
            throw new InvalidPurchaseException("At least one ticket type request is required");
        }

        for (TicketTypeRequest req : request.ticketTypeRequests()) {
            if (req == null || req.getTicketType() == null || req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Each ticket request must have a valid type and positive count");
            }
        }
    }
}
