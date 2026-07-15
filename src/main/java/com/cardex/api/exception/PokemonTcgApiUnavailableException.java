package com.cardex.api.exception;

public class PokemonTcgApiUnavailableException extends RuntimeException {

    public PokemonTcgApiUnavailableException() {
        super("Pokemon TCG API is temporarily unavailable");
    }
}