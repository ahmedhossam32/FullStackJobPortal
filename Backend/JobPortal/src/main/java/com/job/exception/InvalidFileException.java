package com.job.exception;

public class InvalidFileException extends RuntimeException {

    public enum Reason {
        EMPTY,
        TOO_LARGE,
        UNSUPPORTED_TYPE
    }

    private final Reason reason;

    public InvalidFileException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
