package uk.gov.dwp.uc.pairtest.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequestDto(
        @NotNull @Min(1) Long accountId,
        @NotNull @Min(0) @Max(25) Integer adultCount,
        @NotNull @Min(0) @Max(25) Integer childCount,
        @NotNull @Min(0) @Max(25) Integer infantCount
) {
}

