package com.cardex.api.dto.wishlist;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}