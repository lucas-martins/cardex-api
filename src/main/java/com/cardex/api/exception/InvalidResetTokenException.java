package com.cardex.api.exception;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Invalid or expired password reset token.");
    }
}
