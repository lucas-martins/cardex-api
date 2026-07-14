package com.cardex.api.exception;

public class DuplicateCardException extends RuntimeException {

    public DuplicateCardException() {
        super("A card with the same external ID, language and condition already exists");
    }
}