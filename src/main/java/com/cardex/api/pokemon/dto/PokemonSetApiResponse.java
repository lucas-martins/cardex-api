package com.cardex.api.pokemon.dto;

import java.util.List;

public record PokemonSetApiResponse(
        List<PokemonSetApiData> data
) {
}