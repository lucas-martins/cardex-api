package com.cardex.api.pokemon.dto;

public record PokemonCardmarketApiData(
        String url,
        String updatedAt,
        PokemonCardmarketPricesApiData prices
) {
}
