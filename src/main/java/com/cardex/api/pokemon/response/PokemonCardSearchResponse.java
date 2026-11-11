package com.cardex.api.pokemon.response;

import com.cardex.api.enumeration.CardFinish;
import com.cardex.api.enumeration.WishlistPriority;

import java.math.BigDecimal;
import java.util.List;

public record PokemonCardSearchResponse(
        String externalId,
        String name,
        String collectionName,
        String cardNumber,
        String rarity,
        String imageUrl,
        boolean owned,
        Long cardId,
        List<CardFinish> ownedFinishes,
        int ownedQuantity,
        boolean inWishlist,
        Long wishlistId,
        WishlistPriority wishlistPriority,
        BigDecimal marketPriceUsd,
        BigDecimal marketPriceEur,
        BigDecimal marketPriceBrl
) {
}
