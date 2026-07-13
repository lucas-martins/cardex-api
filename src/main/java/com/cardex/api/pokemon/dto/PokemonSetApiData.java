package com.cardex.api.pokemon.dto;

public record PokemonSetApiData(
        String id,
        String name,
        String series,
        Integer printedTotal,
        Integer total
) {
}