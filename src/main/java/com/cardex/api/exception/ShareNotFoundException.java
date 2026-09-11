package com.cardex.api.exception;

public class ShareNotFoundException extends RuntimeException {

    public ShareNotFoundException() {
        super("Shared collection not found.");
    }
}
