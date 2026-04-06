package uk.gov.dwp.uc.pairtest.api;

import java.util.List;

public class ApiValidationException extends RuntimeException {
    private final List<String> ruleViolations;

    public ApiValidationException(List<String> ruleViolations) {
        super("Validation failed");
        this.ruleViolations = ruleViolations;
    }

    public List<String> ruleViolations() {
        return ruleViolations;
    }
}

