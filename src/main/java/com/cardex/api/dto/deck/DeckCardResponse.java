package com.cardex.api.dto.deck;

import com.cardex.api.enumeration.DeckSection;

public record DeckCardResponse(
        Long id,
        String externalId,
        String name,
        String cardNumber,
        String collectionId,
        String collectionName,
        String imageUrl,
        DeckSection section,
        int quantity,
        int ownedQuantity
) {
}
