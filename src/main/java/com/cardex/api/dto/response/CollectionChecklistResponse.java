package com.cardex.api.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CollectionChecklistResponse(
        String collectionId,
        String collectionName,
        long ownedUniqueCards,
        long totalCards,
        double completionPercentage,
        long ownedNumberedCards,
        long numberedCards,
        long ownedAdditionalCards,
        long additionalCards,
        BigDecimal estimatedOwnedValueUsd,
        BigDecimal estimatedOwnedValueEur,
        BigDecimal estimatedMissingValueUsd,
        BigDecimal estimatedMissingValueEur,
        List<CollectionChecklistCardResponse> cards
) {
}