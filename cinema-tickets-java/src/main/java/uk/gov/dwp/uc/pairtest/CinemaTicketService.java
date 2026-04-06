package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt;

/**
 * Extension point for the web/API layer.
 *
 * The original kata constraint is preserved: {@link TicketService} remains unchanged.
 */
public interface CinemaTicketService extends TicketService {

    PurchaseReceipt purchaseTicketsWithReceipt(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException;
}

