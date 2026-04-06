package uk.gov.dwp.uc.pairtest.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import uk.gov.dwp.uc.pairtest.exception.PaymentFailedException;

@Component
public class ThirdPartyTicketPaymentService implements TicketPaymentService {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyTicketPaymentService.class);

    private final TicketPaymentService delegate;

    public ThirdPartyTicketPaymentService() {
        this(new TicketPaymentServiceImpl());
    }

    ThirdPartyTicketPaymentService(TicketPaymentService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void makePayment(long accountId, int totalAmountToPay) {
        try {
            delegate.makePayment(accountId, totalAmountToPay);
        } catch (Exception e) {
            log.error("Payment failed (accountId={}, amount={})", accountId, totalAmountToPay, e);
            throw new PaymentFailedException("Payment failed", e);
        }
    }
}

