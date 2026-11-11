package com.cardex.api.dto.deck;

import java.time.LocalDateTime;
import java.util.List;

public record DeckResponse(
        Long id,
        String name,
        String notes,
        int totalCards,
        int uniqueCards,
        List<DeckCardResponse> cards,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
