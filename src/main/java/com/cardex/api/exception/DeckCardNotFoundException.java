package com.cardex.api.exception;

public class DeckCardNotFoundException extends RuntimeException {

    public DeckCardNotFoundException(Long id) {
        super("Deck card not found: " + id);
    }
}
