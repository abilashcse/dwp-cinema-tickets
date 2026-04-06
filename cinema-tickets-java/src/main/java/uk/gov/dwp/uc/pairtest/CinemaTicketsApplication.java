package uk.gov.dwp.uc.pairtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CinemaTicketsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinemaTicketsApplication.class, args);
    }
}

