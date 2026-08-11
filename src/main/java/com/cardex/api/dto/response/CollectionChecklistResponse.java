package com.cardex.api.dto.response;

import java.util.List;

public record CollectionChecklistResponse(
        String collectionId,
        String collectionName,
        long ownedUniqueCards,
        long totalCards,
        double completionPercentage,
        List<CollectionChecklistCardResponse> cards
) {
}