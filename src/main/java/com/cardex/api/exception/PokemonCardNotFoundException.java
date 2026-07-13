package com.cardex.api.exception;

public class PokemonCardNotFoundException extends RuntimeException {

    public PokemonCardNotFoundException(String externalId) {
        super("Pokemon card not found for external ID: " + externalId);
    }
}