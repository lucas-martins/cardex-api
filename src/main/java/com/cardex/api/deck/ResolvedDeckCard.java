package com.cardex.api.deck;

import com.cardex.api.enumeration.DeckSection;

public record ResolvedDeckCard(
        boolean resolved,
        int lineNumber,
        String rawLine,
        DeckSection section,
        int quantity,
        String requestedName,
        String setAbbreviation,
        String cardNumber,
        String externalId,
        String name,
        String collectionId,
        String collectionName,
        String imageUrl,
        int ownedQuantity
) {
}
