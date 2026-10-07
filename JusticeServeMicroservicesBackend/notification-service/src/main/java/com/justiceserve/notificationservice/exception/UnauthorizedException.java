package com.justiceserve.notificationservice.exception;

public class UnauthorizedException  extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
