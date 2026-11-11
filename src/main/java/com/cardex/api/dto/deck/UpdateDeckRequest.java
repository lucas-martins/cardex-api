package com.cardex.api.dto.deck;

import jakarta.validation.constraints.Size;

public record UpdateDeckRequest(
        @Size(max = 150) String name,
        @Size(max = 1000) String notes
) {
}
