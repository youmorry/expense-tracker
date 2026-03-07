package com.youmorry.expensetracker.shared.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String detail) {
        super("about:blank", "Not Found", 404, detail);
    }
}
