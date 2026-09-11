package com.cardex.api.pokemon.dto;

import java.util.Map;

public record PokemonTcgPlayerApiData(
        String url,
        String updatedAt,
        Map<String, PokemonTcgPlayerPriceApiData> prices
) {
}
