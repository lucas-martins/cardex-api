package com.cardex.api.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Long id) {
        super("Card not found for ID: " + id);
    }
}