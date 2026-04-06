package uk.gov.dwp.uc.pairtest.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        @DefaultValue("50") long capacity,
        @DefaultValue("50") long refillTokens,
        @DefaultValue("PT1M") Duration refillPeriod
) {
}

