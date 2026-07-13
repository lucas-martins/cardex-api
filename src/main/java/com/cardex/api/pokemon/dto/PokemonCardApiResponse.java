package com.cardex.api.pokemon.dto;

import java.util.List;

public record PokemonCardApiResponse(
        List<PokemonCardApiData> data,
        Integer page,
        Integer pageSize,
        Integer count,
        Integer totalCount
) {
}