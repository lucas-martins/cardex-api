package com.cardex.api.dto.response;

public record ShareStatusResponse(
        boolean enabled,
        String shareToken,
        String shareUrl
) {
}
