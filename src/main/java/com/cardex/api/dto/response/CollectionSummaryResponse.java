package com.cardex.api.dto.response;

import java.math.BigDecimal;

public record CollectionSummaryResponse(
        long uniqueCards,
        long totalCards,
        long differentLanguages,
        long differentCollections,
        MostOwnedCardResponse mostOwnedCard,
        BigDecimal estimatedValueUsd,
        BigDecimal estimatedValueEur,
        BigDecimal estimatedValueBrl,
        long pricedCopies,
        long unpricedCopies
) {
}