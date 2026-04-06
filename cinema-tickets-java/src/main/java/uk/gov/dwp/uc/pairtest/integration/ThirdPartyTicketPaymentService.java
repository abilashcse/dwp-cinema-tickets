package uk.gov.dwp.uc.pairtest.integration;

import org.springframework.stereotype.Component;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;

@Component
public class ThirdPartyTicketPaymentService implements TicketPaymentService {

    private final TicketPaymentService delegate = new TicketPaymentServiceImpl();

    @Override
    public void makePayment(long accountId, int totalAmountToPay) {
        delegate.makePayment(accountId, totalAmountToPay);
    }
}

