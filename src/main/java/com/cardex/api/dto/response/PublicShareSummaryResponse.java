package com.cardex.api.dto.response;

import java.util.List;

public record PublicShareSummaryResponse(
        String ownerName,
        String shareToken,
        List<PublicShareCollectionResponse> collections
) {
}
