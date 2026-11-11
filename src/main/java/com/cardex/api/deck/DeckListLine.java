package com.cardex.api.deck;

import com.cardex.api.enumeration.DeckSection;

public record DeckListLine(
        int lineNumber,
        String rawLine,
        DeckSection section,
        int quantity,
        String name,
        String setAbbreviation,
        String cardNumber
) {
}
