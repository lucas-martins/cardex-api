package com.cardex.api.dto.response;

public record CollectionChecklistCardResponse(
        String externalId,
        String name,
        String cardNumber,
        String rarity,
        String imageUrl,
        boolean owned,
        Long cardId
) {
}