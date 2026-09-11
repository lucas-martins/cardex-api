package com.cardex.api.dto.response;

import java.math.BigDecimal;

public record CollectionValueItemResponse(
        String name,
        BigDecimal estimatedValueUsd,
        BigDecimal estimatedValueEur
) {
}
