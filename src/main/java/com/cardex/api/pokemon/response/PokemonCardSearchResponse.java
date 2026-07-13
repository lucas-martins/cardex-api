package com.cardex.api.pokemon.response;

public record PokemonCardSearchResponse(
        String externalId,
        String name,
        String collectionName,
        String cardNumber,
        String rarity,
        String imageUrl
) {
}