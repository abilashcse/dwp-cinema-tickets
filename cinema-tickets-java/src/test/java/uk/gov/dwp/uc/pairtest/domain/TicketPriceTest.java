package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.dwp.uc.pairtest.config.TicketPricingProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TicketPriceTest {

    @Autowired
    private TicketPricingProperties pricing;

    @Test
    void loadsTicketPricesFromApplicationProperties() {
        assertEquals(25, pricing.adult());
        assertEquals(15, pricing.child());
        assertEquals(0, pricing.infant());
    }
}
