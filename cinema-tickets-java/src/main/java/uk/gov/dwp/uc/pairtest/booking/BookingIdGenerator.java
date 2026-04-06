package uk.gov.dwp.uc.pairtest.booking;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BookingIdGenerator {
    private final AtomicLong counter = new AtomicLong(100);

    public String nextId() {
        return "T-" + counter.getAndIncrement();
    }
}

