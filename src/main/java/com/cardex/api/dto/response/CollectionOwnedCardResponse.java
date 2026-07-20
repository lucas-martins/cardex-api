package com.cardex.api.dto.response;

public record CollectionOwnedCardResponse(
        Long id,
        String externalId,
        String name,
        String cardNumber,
        String rarity,
        String imageUrl,
        int quantity,
        String language,
        String condition,
        boolean favorite
) {
}