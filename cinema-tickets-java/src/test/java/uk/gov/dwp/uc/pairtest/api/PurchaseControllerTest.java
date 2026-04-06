package uk.gov.dwp.uc.pairtest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.exception.PaymentFailedException;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PurchaseController.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
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
    void returns201AndCallsTicketService() throws Exception {
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new PurchaseSummary(2, 1, 1, 4, 50, 3));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":2,"childCount":1,"infantCount":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(123))
                .andExpect(jsonPath("$.totalAmountToPay").value(50))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3))
                .andExpect(jsonPath("$.totalTickets").value(4));

        verify(ticketService).purchaseTickets(anyLong(), any(TicketTypeRequest[].class));
    }

    @Test
    void returns400WhenBodyIsEmpty() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns415ForUnsupportedMediaType() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Unsupported Content-Type. Expected application/json"));
    }

    @Test
    void returns405ForMethodNotAllowed() throws Exception {
        mvc.perform(get("/api/purchases"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Method not allowed"));
    }

    @Test
    void returns500ForUnexpectedException() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(ticketService)
                .purchaseTickets(eq(123L), any(TicketTypeRequest[].class));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void returns500ForPaymentFailure() throws Exception {
        doThrow(new PaymentFailedException("Payment failed", new RuntimeException("boom")))
                .when(ticketService)
                .purchaseTickets(eq(123L), any(TicketTypeRequest[].class));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Payment processing failed"));
    }

    @Test
    void returns400WhenFieldsAreMissing() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenNegativeCountsProvided() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":-1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenTotalTicketsExceed25() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":20,"childCount":5,"infantCount":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations[0]").value("Total tickets must be <= 25"));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenAllCountsAreZero() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":0,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations[0]").value("At least one ticket must be purchased"));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenInfantsExceedAdults() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":1,"childCount":0,"infantCount":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations", hasItem("Number of infants cannot exceed number of adults")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns201ForAdultsOnly() throws Exception {
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new PurchaseSummary(3, 0, 0, 3, 60, 3));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":3,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adults").value(3))
                .andExpect(jsonPath("$.totalAmountToPay").value(60))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3))
                .andExpect(jsonPath("$.totalTickets").value(3));
    }

    @Test
    void infantsDoNotContributeToPaymentOrSeats() throws Exception {
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new PurchaseSummary(2, 0, 2, 4, 40, 2));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":2,"childCount":0,"infantCount":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmountToPay").value(40))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(2))
                .andExpect(jsonPath("$.totalTickets").value(4));
    }
}
