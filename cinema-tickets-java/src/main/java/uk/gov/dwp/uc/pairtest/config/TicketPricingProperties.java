package uk.gov.dwp.uc.pairtest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ticket-pricing")
public record TicketPricingProperties(
        @DefaultValue("25") int adult,
        @DefaultValue("15") int child,
        @DefaultValue("0") int infant
) {
}

