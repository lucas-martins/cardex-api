package com.cardex.api.dto.deck;

import jakarta.validation.constraints.NotBlank;

public record ParseDeckListRequest(
        @NotBlank String listText
) {
}
