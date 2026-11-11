package com.cardex.api.dto.deck;

import java.time.LocalDateTime;

public record DeckSummaryResponse(
        Long id,
        String name,
        String notes,
        int totalCards,
        int uniqueCards,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
