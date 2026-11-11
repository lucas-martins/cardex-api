package com.cardex.api.pokemon.dto;

public record PokemonSetApiData(
        String id,
        String name,
        String series,
        Integer printedTotal,
        Integer total,
        String abbreviation
) {
    public PokemonSetApiData(
            String id,
            String name,
            String series,
            Integer printedTotal,
            Integer total
    ) {
        this(id, name, series, printedTotal, total, null);
    }
}