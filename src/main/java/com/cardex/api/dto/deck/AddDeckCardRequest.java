package com.cardex.api.dto.deck;

import com.cardex.api.enumeration.DeckSection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddDeckCardRequest(
        @NotBlank String externalId,
        @NotNull DeckSection section,
        @NotNull @Min(1) Integer quantity
) {
}
