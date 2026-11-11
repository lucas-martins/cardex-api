package com.cardex.api.dto.deck;

import java.util.List;

public record DeckParseResponse(
        int totalLines,
        int resolvedCount,
        int unresolvedCount,
        List<DeckCardResponse> resolvedCards,
        List<UnresolvedDeckCardResponse> unresolvedCards
) {
}
