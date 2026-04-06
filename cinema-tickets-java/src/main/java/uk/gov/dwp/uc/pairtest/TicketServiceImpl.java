package uk.gov.dwp.uc.pairtest;

import org.springframework.stereotype.Service;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.Purchase;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.repository.NoOpPurchaseRepository;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.AccountIdValidator;
import uk.gov.dwp.uc.pairtest.validation.BusinessRulesValidator;
import uk.gov.dwp.uc.pairtest.validation.PurchaseContext;
import uk.gov.dwp.uc.pairtest.validation.PurchaseRequest;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;
import uk.gov.dwp.uc.pairtest.validation.TicketTypeRequestsValidator;

@Service
public class TicketServiceImpl implements TicketService {
   

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private TicketPaymentService ticketPaymentService;

    private SeatReservationService seatReservationService;

    private PurchaseRepository purchaseRepository = new NoOpPurchaseRepository();

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService,
                             PurchaseRepository purchaseRepository) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.purchaseRepository = purchaseRepository == null ? new NoOpPurchaseRepository() : purchaseRepository;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        PurchaseRequest request = new PurchaseRequest(accountId, ticketTypeRequests);
        new AccountIdValidator().validate(request);
        new TicketTypeRequestsValidator().validate(request);

        PurchaseSummary summary = summarize(ticketTypeRequests);
        new BusinessRulesValidator(MAX_TICKETS_PER_PURCHASE).validate(new PurchaseContext(accountId, ticketTypeRequests, summary));

        ticketPaymentService.makePayment(accountId, summary.totalAmountToPay());
        seatReservationService.reserveSeat(accountId, summary.totalSeatsToAllocate());

        purchaseRepository.save(Purchase.of(
                accountId,
                summary.adults(),
                summary.children(),
                summary.infants(),
                summary.totalTickets(),
                summary.totalAmountToPay(),
                summary.totalSeatsToAllocate()
        ));
    }

    private static PurchaseSummary summarize(TicketTypeRequest... ticketTypeRequests) {
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
        int totalAmountToPay = (adults * TicketPrice.ADULT.price()) + (children * TicketPrice.CHILD.price());
        int totalSeatsToAllocate = adults + children;

        return new PurchaseSummary(adults, children, infants, totalTickets, totalAmountToPay, totalSeatsToAllocate);
    }

}
