package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PurchaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void fullPurchaseFlowReturns200WithCorrectTotals() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":42,"adultCount":2,"childCount":1,"infantCount":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(42))
                .andExpect(jsonPath("$.adults").value(2))
                .andExpect(jsonPath("$.children").value(1))
                .andExpect(jsonPath("$.infants").value(1))
                .andExpect(jsonPath("$.totalTickets").value(4))
                .andExpect(jsonPath("$.totalAmountToPay").value(50))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3));
    }

    @Test
    void purchaseWithInvalidAccountIdReturns400() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":0,"adultCount":1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchaseExceedingMaxTicketsReturns400() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":20,"childCount":5,"infantCount":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations[0]").value("Total tickets must be <= 25"));
    }

    @Test
    void purchaseChildWithoutAdultReturns400() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":0,"childCount":2,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations[0]").value("Child and/or infant tickets require at least 1 adult ticket"));
    }

    @Test
    void ticketRatesEndpointReturnsPricesAndMax() throws Exception {
        mvc.perform(get("/api/ticket-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTicketsPerPurchase").value(25))
                .andExpect(jsonPath("$.rates[0].type").value("ADULT"))
                .andExpect(jsonPath("$.rates[0].price").value(20))
                .andExpect(jsonPath("$.rates[1].type").value("CHILD"))
                .andExpect(jsonPath("$.rates[1].price").value(10))
                .andExpect(jsonPath("$.rates[2].type").value("INFANT"))
                .andExpect(jsonPath("$.rates[2].price").value(0));
    }

    @Test
    void unsupportedMediaTypeReturnsApiErrorResponse() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported Content-Type. Expected application/json"));
    }

    @Test
    void malformedJsonReturnsApiErrorResponse() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or missing JSON request body"));
    }
}
