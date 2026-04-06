package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketPriceTest {

    @Test
    void adultPriceIs20() {
        assertEquals(20, TicketPrice.ADULT.price());
    }

    @Test
    void childPriceIs10() {
        assertEquals(10, TicketPrice.CHILD.price());
    }

    @Test
    void infantPriceIs0() {
        assertEquals(0, TicketPrice.INFANT.price());
    }

    @ParameterizedTest
    @CsvSource({"ADULT, 20", "CHILD, 10", "INFANT, 0"})
    void fromMapsTypeToCorrectPrice(TicketTypeRequest.Type type, int expectedPrice) {
        assertEquals(expectedPrice, TicketPrice.from(type).price());
    }

    @ParameterizedTest
    @CsvSource({"ADULT, ADULT", "CHILD, CHILD", "INFANT, INFANT"})
    void fromMapsTypeToMatchingEnum(TicketTypeRequest.Type type, TicketPrice expectedPrice) {
        assertEquals(expectedPrice, TicketPrice.from(type));
    }
}
