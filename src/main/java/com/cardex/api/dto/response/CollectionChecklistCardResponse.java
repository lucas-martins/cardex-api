package com.cardex.api.dto.response;

import com.cardex.api.enumeration.CardCollectionSection;
import com.cardex.api.enumeration.WishlistPriority;

import java.math.BigDecimal;

public record CollectionChecklistCardResponse(
        String externalId,
        String name,
        String cardNumber,
        String rarity,
        String imageUrl,
        boolean owned,
        Long cardId,
        boolean inWishlist,
        Long wishlistId,
        WishlistPriority wishlistPriority,
        CardCollectionSection section,
        BigDecimal marketPriceUsd,
        BigDecimal marketPriceEur
) {
}