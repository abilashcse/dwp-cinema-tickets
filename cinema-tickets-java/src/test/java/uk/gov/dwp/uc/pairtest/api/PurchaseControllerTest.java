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
import uk.gov.dwp.uc.pairtest.exception.SeatReservationFailedException;
import uk.gov.dwp.uc.pairtest.repository.PurchaseRepository;
import uk.gov.dwp.uc.pairtest.validation.PurchaseSummary;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PurchaseController.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private PurchaseRepository purchaseRepository;

    @Test
    void returns400ForBeanValidationFailures() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":0,"adultCount":0,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("accountId")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400ForBusinessRuleViolations() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":0,"childCount":1,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenTicketServiceRejectsPurchase() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        when(purchaseRepository.findAll()).thenReturn(List.of());
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt(
                        "T-100",
                        new PurchaseSummary(2, 1, 1, 4, 65, 3)
                ));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":2,"childCount":1,"infantCount":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("T-100"))
                .andExpect(jsonPath("$.accountId").value(123))
                .andExpect(jsonPath("$.totalAmountToPay").value(65))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3))
                .andExpect(jsonPath("$.totalTickets").value(4))
                .andExpect(jsonPath("$.message").value("Purchase confirmed"));

        verify(ticketService).purchaseTickets(anyLong(), any(TicketTypeRequest[].class));
    }

    @Test
    void returns400WhenBodyIsEmpty() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        mvc.perform(put("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Method not allowed"));
    }

    @Test
    void returns500ForUnexpectedException() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
    void returns500ForSeatReservationFailure() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        doThrow(new SeatReservationFailedException("Seat reservation failed", new RuntimeException("boom")))
                .when(ticketService)
                .purchaseTickets(eq(123L), any(TicketTypeRequest[].class));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":1,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Seat reservation failed"));
    }

    @Test
    void returns400WhenFieldsAreMissing() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
    void returns400WhenCountExceedsMax() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":26,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400ForHugeCountsThatCouldOverflowInt() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":2147483647,"childCount":1,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenTotalTicketsExceed25() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        when(purchaseRepository.findAll()).thenReturn(List.of());
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":123,"adultCount":0,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("adultCount")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void returns400WhenInfantsExceedAdults() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
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
        when(purchaseRepository.findAll()).thenReturn(List.of());
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt(
                        "b-2",
                        new PurchaseSummary(3, 0, 0, 3, 75, 3)
                ));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":3,"childCount":0,"infantCount":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("b-2"))
                .andExpect(jsonPath("$.adults").value(3))
                .andExpect(jsonPath("$.totalAmountToPay").value(75))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(3))
                .andExpect(jsonPath("$.totalTickets").value(3))
                .andExpect(jsonPath("$.message").value("Purchase confirmed"));
    }

    @Test
    void infantsDoNotContributeToPaymentOrSeats() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt(
                        "b-3",
                        new PurchaseSummary(2, 0, 2, 4, 50, 2)
                ));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":2,"childCount":0,"infantCount":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("b-3"))
                .andExpect(jsonPath("$.totalAmountToPay").value(50))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(2))
                .andExpect(jsonPath("$.totalTickets").value(4))
                .andExpect(jsonPath("$.message").value("Purchase confirmed"));
    }

    @Test
    void oneAdultOneInfantOnLap() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());
        when(ticketService.purchaseTickets(anyLong(), any(TicketTypeRequest[].class)))
                .thenReturn(new uk.gov.dwp.uc.pairtest.validation.PurchaseReceipt(
                        "b-4",
                        new PurchaseSummary(1, 0, 1, 2, 25, 1)
                ));

        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":1,"childCount":0,"infantCount":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("b-4"))
                .andExpect(jsonPath("$.adults").value(1))
                .andExpect(jsonPath("$.infants").value(1))
                .andExpect(jsonPath("$.totalAmountToPay").value(25))
                .andExpect(jsonPath("$.totalSeatsToAllocate").value(1))
                .andExpect(jsonPath("$.totalTickets").value(2))
                .andExpect(jsonPath("$.message").value("Purchase confirmed"));
    }

    @Test
    void rejectsOneAdultWithTwoInfants() throws Exception {
        mvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountId":1,"adultCount":1,"childCount":0,"infantCount":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ruleViolations", hasItem("Number of infants cannot exceed number of adults")));

        verify(ticketService, never()).purchaseTickets(anyLong(), any());
    }

    @Test
    void getAllPurchasesReturnsEmptyListWhenNoPurchases() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of());

        mvc.perform(get("/api/purchases").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllPurchasesReturnsSavedPurchases() throws Exception {
        when(purchaseRepository.findAll()).thenReturn(List.of(
                new uk.gov.dwp.uc.pairtest.domain.Purchase(
                        "b-10", 1L, 2, 1, 0, 3, 65, 3, Instant.parse("2026-04-06T12:00:00Z")
                ),
                new uk.gov.dwp.uc.pairtest.domain.Purchase(
                        "b-11", 2L, 1, 0, 1, 2, 25, 1, Instant.parse("2026-04-06T13:00:00Z")
                )
        ));

        mvc.perform(get("/api/purchases").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].bookingId").value("b-10"))
                .andExpect(jsonPath("$[0].accountId").value(1))
                .andExpect(jsonPath("$[0].totalAmountToPay").value(65))
                .andExpect(jsonPath("$[1].bookingId").value("b-11"))
                .andExpect(jsonPath("$[1].accountId").value(2))
                .andExpect(jsonPath("$[1].totalAmountToPay").value(25));
    }
}
