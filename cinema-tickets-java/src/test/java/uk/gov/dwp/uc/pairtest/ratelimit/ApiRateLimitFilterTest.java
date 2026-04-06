package uk.gov.dwp.uc.pairtest.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "feature.rate-limit.enabled=true",
        "rate-limit.capacity=2",
        "rate-limit.refill-tokens=2",
        "rate-limit.refill-period=PT1H"
})
class ApiRateLimitFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void returns429WhenRateLimitExceeded() throws Exception {
        mvc.perform(get("/api/ticket-rates"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/ticket-rates"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/ticket-rates"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded"));
    }

    @Test
    void doesNotApplyToNonApiPaths() throws Exception {
        mvc.perform(get("/actuator/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

