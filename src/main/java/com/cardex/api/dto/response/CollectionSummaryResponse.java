package com.cardex.api.dto.response;

public record CollectionSummaryResponse(
        long uniqueCards,
        long totalCards,
        long differentLanguages,
        long differentCollections,
        MostOwnedCardResponse mostOwnedCard
) {
}