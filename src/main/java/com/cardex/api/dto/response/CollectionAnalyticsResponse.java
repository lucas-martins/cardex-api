package com.cardex.api.dto.response;

import java.util.List;

public record CollectionAnalyticsResponse(
        List<CollectionAnalyticsItemResponse> collections,
        List<CollectionAnalyticsItemResponse> languages,
        List<CollectionAnalyticsItemResponse> conditions,
        List<CollectionAnalyticsItemResponse> rarities
) {
}