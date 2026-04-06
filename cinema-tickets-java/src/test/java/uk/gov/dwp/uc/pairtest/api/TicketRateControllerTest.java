package uk.gov.dwp.uc.pairtest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.dwp.uc.pairtest.config.TicketPricingProperties;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketRateController.class)
@EnableConfigurationProperties(TicketPricingProperties.class)
@TestPropertySource(properties = {
        "ticket-pricing.adult=25",
        "ticket-pricing.child=15",
        "ticket-pricing.infant=0"
})
class TicketRateControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void returnsAllRatesAndMaxTickets() throws Exception {
        mvc.perform(get("/api/ticket-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTicketsPerPurchase").value(25))
                .andExpect(jsonPath("$.rates", hasSize(3)))
                .andExpect(jsonPath("$.rates[0].type").value("ADULT"))
                .andExpect(jsonPath("$.rates[0].price").value(25))
                .andExpect(jsonPath("$.rates[0].requiresSeat").value(true))
                .andExpect(jsonPath("$.rates[1].type").value("CHILD"))
                .andExpect(jsonPath("$.rates[1].price").value(15))
                .andExpect(jsonPath("$.rates[1].requiresSeat").value(true))
                .andExpect(jsonPath("$.rates[2].type").value("INFANT"))
                .andExpect(jsonPath("$.rates[2].price").value(0))
                .andExpect(jsonPath("$.rates[2].requiresSeat").value(false));
    }
}
