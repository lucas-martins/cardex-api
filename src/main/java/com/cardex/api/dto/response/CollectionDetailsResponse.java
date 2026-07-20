package com.cardex.api.dto.response;

import java.util.List;

public record CollectionDetailsResponse(
        String collectionId,
        String collectionName,
        long ownedUniqueCards,
        long totalCards,
        double completionPercentage,
        List<CollectionOwnedCardResponse> cards
) {
}