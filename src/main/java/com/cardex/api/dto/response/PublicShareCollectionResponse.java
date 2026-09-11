package com.cardex.api.dto.response;

public record PublicShareCollectionResponse(
        String id,
        String name,
        String series,
        Integer printedTotal,
        Integer total,
        long ownedCards,
        double completionPercentage
) {
}
