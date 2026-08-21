package com.cardex.api.pokemon.response;

import com.cardex.api.enumeration.WishlistPriority;

public record PokemonCardSearchResponse(
        String externalId,
        String name,
        String collectionName,
        String cardNumber,
        String rarity,
        String imageUrl,
        boolean owned,
        Long cardId,
        boolean inWishlist,
        Long wishlistId,
        WishlistPriority wishlistPriority
) {
}