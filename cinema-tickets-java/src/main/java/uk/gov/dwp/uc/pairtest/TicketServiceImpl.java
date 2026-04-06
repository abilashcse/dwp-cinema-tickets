package uk.gov.dwp.uc.pairtest;

import org.springframework.stereotype.Service;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.config.TicketPricingProperties;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.AccountIdValidator;
import uk.gov.dwp.uc.pairtest.validation.BusinessRulesValidator;
import uk.gov.dwp.uc.pairtest.validation.PurchaseRequest;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;
import uk.gov.dwp.uc.pairtest.validation.TicketTypeRequestsValidator;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private final PurchaseRepository purchaseRepository;
    private final TicketPricingProperties ticketPricing;
    private final AccountIdValidator accountIdValidator;
    private final TicketTypeRequestsValidator ticketTypeRequestsValidator;
    private final BusinessRulesValidator businessRulesValidator;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService,
                             PurchaseRepository purchaseRepository,
                             TicketPricingProperties ticketPricing,
                             AccountIdValidator accountIdValidator,
                             TicketTypeRequestsValidator ticketTypeRequestsValidator,
                             BusinessRulesValidator businessRulesValidator) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.purchaseRepository = purchaseRepository;
        this.ticketPricing = ticketPricing;
        this.accountIdValidator = accountIdValidator;
        this.ticketTypeRequestsValidator = ticketTypeRequestsValidator;
        this.businessRulesValidator = businessRulesValidator;
    }

    @Override
    public PurchaseSummary purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        PurchaseRequest request = new PurchaseRequest(accountId, ticketTypeRequests);
        accountIdValidator.validate(request);
        ticketTypeRequestsValidator.validate(request);

        PurchaseSummary summary = summarize(ticketTypeRequests, ticketPricing);
        businessRulesValidator.validate(summary);

        seatReservationService.reserveSeat(accountId, summary.totalSeatsToAllocate());
        ticketPaymentService.makePayment(accountId, summary.totalAmountToPay());

        purchaseRepository.save(Purchase.of(
                accountId,
                summary.adults(),
                summary.children(),
                summary.infants(),
                summary.totalTickets(),
                summary.totalAmountToPay(),
                summary.totalSeatsToAllocate()
        ));

        return summary;
    }

    private static PurchaseSummary summarize(TicketTypeRequest[] ticketTypeRequests, TicketPricingProperties ticketPricing) {
        int adults = 0;
        int children = 0;
        int infants = 0;

        for (TicketTypeRequest req : ticketTypeRequests) {
            switch (req.getTicketType()) {
                case ADULT -> adults += req.getNoOfTickets();
                case CHILD -> children += req.getNoOfTickets();
                case INFANT -> infants += req.getNoOfTickets();
            }
        }

        int totalTickets = adults + children + infants;
        int totalAmountToPay = (adults * ticketPricing.adult()) + (children * ticketPricing.child());
        int totalSeatsToAllocate = adults + children;

        return new PurchaseSummary(adults, children, infants, totalTickets, totalAmountToPay, totalSeatsToAllocate);
    }

}
