package com.youmorry.expensetracker.shared.exception;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String detail) {
        super("/errors/unauthorized", "Authentication required.", 401, detail);
    }
}
