package com.youmorry.expensetracker.shared.exception;

public class ForbiddenException extends AppException {

    public ForbiddenException(String detail) {
        super("/errors/forbidden", "Forbidden.", 403, detail);
    }
}
