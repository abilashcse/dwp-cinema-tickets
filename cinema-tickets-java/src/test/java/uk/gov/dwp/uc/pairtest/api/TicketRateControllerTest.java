package uk.gov.dwp.uc.pairtest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketRateController.class)
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
                .andExpect(jsonPath("$.rates[0].price").value(20))
                .andExpect(jsonPath("$.rates[0].requiresSeat").value(true))
                .andExpect(jsonPath("$.rates[1].type").value("CHILD"))
                .andExpect(jsonPath("$.rates[1].price").value(10))
                .andExpect(jsonPath("$.rates[1].requiresSeat").value(true))
                .andExpect(jsonPath("$.rates[2].type").value("INFANT"))
                .andExpect(jsonPath("$.rates[2].price").value(0))
                .andExpect(jsonPath("$.rates[2].requiresSeat").value(false));
    }
}
