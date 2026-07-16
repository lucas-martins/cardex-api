package com.cardex.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCardFavoriteRequest(
        @NotNull(message = "Favorite status is required")
        Boolean favorite
) {
}