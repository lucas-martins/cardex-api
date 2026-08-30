package com.cardex.api.pokemon.response;

public record PokemonCollectionResponse(
        String id,
        String name,
        String series,
        Integer printedTotal,
        Integer total,
        long ownedCards,
        double completionPercentage
) {
}