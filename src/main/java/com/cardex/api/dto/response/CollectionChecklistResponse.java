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
        BigDecimal estimatedOwnedValueBrl,
        BigDecimal estimatedMissingValueUsd,
        BigDecimal estimatedMissingValueEur,
        BigDecimal estimatedMissingValueBrl,
        List<CollectionChecklistCardResponse> cards
) {
}