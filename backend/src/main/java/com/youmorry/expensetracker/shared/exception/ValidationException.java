package com.youmorry.expensetracker.shared.exception;

import java.util.List;

public class ValidationException extends AppException {

    private final List<FieldError> errors;

    public ValidationException(String detail, List<FieldError> errors) {
        super("/errors/validation-error", "Your request is not valid.", 422, detail);
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public record FieldError(String detail, String pointer) {}
}
