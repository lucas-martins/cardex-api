package com.cardex.api.dto.response;

import com.cardex.api.enumeration.CardCollectionSection;
import com.cardex.api.enumeration.CardFinish;
import com.cardex.api.enumeration.WishlistPriority;

import java.math.BigDecimal;
import java.util.List;

public record CollectionChecklistCardResponse(
        String externalId,
        String name,
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
        CardCollectionSection section,
        BigDecimal marketPriceUsd,
        BigDecimal marketPriceEur,
        BigDecimal marketPriceBrl
) {
}
