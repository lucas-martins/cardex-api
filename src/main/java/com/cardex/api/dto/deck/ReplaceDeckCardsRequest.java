package com.cardex.api.dto.deck;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReplaceDeckCardsRequest(
        @NotEmpty @Valid List<DeckCardInput> cards
) {
}
