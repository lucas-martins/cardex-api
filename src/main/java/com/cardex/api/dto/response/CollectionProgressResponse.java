package com.cardex.api.dto.response;

import java.math.BigDecimal;

public record CollectionProgressResponse(
        String collectionId,
        String collectionName,
        long ownedCards,
        long totalCards,
        double completionPercentage,
        BigDecimal estimatedValueUsd,
        BigDecimal estimatedValueEur
) {
}