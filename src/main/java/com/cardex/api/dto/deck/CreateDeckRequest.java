package com.cardex.api.dto.deck;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDeckRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String notes,
        @NotEmpty @Valid List<DeckCardInput> cards
) {
}
