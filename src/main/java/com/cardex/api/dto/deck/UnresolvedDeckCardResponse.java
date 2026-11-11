package com.cardex.api.dto.deck;

import com.cardex.api.enumeration.DeckSection;

public record UnresolvedDeckCardResponse(
        int lineNumber,
        String rawLine,
        DeckSection section,
        int quantity,
        String requestedName,
        String setAbbreviation,
        String cardNumber
) {
}
