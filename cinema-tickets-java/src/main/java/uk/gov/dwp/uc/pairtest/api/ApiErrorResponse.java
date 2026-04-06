package uk.gov.dwp.uc.pairtest.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<ApiFieldError> fieldErrors,
        List<String> ruleViolations
) {
    public record ApiFieldError(String field, String message) {
    }
}

