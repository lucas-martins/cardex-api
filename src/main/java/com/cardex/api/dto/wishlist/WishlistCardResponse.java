package com.cardex.api.dto.wishlist;

import com.cardex.api.enumeration.WishlistPriority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WishlistCardResponse(
        Long id,
        String externalId,
        String name,
        String cardNumber,
        String collectionId,
        String collectionName,
        String series,
        String rarity,
        List<String> types,
        String imageUrl,
        WishlistPriority priority,
        String notes,
        String storeUrl,
        BigDecimal targetPriceUsd,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal marketPriceUsd,
        BigDecimal marketPriceEur,
        BigDecimal marketPriceBrl
) {
}
