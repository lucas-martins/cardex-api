package com.cardex.api.dto.wishlist;

import com.cardex.api.enumeration.WishlistPriority;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistCardResponse(
        Long id,
        String externalId,
        String name,
        String cardNumber,
        String collectionId,
        String collectionName,
        String series,
        String rarity,
        String imageUrl,
        WishlistPriority priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal marketPriceUsd,
        BigDecimal marketPriceEur
) {
}