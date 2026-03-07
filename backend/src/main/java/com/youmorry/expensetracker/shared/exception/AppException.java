package com.youmorry.expensetracker.shared.exception;

public abstract class AppException extends RuntimeException {

    private final String type;
    private final String title;
    private final int status;

    protected AppException(String type, String title, int status, String detail) {
        super(detail);
        this.type = type;
        this.title = title;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return getMessage();
    }
}
