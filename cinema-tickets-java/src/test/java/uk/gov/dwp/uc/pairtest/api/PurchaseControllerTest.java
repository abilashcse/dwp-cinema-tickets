package uk.gov.dwp.uc.pairtest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PurchaseController.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TicketService ticketService;

    @Test
    void returns400ForBeanValidationFailures() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":0,"adultCount":0,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("accountId")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400ForBusinessRuleViolations() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":0,"childCount":1,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations[0]").value("Child and/or infant tickets require at least 1 adult ticket"));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenTicketServiceRejectsPurchase() throws Exception {
        doThrow(new InvalidPurchaseException())
                .when(ticketService)
                .purchaseTickets(eq(123L), any(TicketTypeRequest[].class));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid purchase"));
    }

    @Test
    void returns200AndCallsTicketService() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":2,"childCount":1,"infantCount":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(123))
                .andExpect(jsonPath("$.totalAmountToPay").value(50))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3))
                .andExpect(jsonPath("$.totalTickets").value(4));

        verify(ticketService).purchaseTickets(anyLong(), any(TicketTypeRequest[].class));
    }
}

