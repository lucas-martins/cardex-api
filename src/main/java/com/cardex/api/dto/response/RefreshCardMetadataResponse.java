package com.cardex.api.dto.response;

public record RefreshCardMetadataResponse(
        long processedCards,
        long updatedCards
) {
}