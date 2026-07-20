package com.cardex.api.dto.response;

public record CollectionProgressResponse(
        String collectionId,
        String collectionName,
        long ownedCards,
        long totalCards,
        double completionPercentage
) {
}