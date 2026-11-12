package fr.lis.ikeyplus.rest;

import java.util.Map;

public class InputValidationException extends RuntimeException {

    private final Map<String, String> details;

    public InputValidationException(final Map<String, String> details) {
        super("Invalid Input");
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
