package uk.gov.dwp.uc.pairtest.integration;

import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import uk.gov.dwp.uc.pairtest.exception.PaymentFailedException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThirdPartyTicketPaymentServiceTest {

    @Test
    void wrapsDelegateExceptions() {
        TicketPaymentService throwingDelegate = new TicketPaymentService() {
            @Override
            public void makePayment(long accountId, int totalAmountToPay) {
                throw new RuntimeException("boom");
            }
        };
        ThirdPartyTicketPaymentService service = new ThirdPartyTicketPaymentService(throwingDelegate);

        assertThrows(PaymentFailedException.class, () -> service.makePayment(1L, 25));
    }
}

