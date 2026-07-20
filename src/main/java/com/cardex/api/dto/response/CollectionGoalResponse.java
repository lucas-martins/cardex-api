package com.cardex.api.dto.response;

public record CollectionGoalResponse(
        String code,
        String title,
        String description,
        long currentValue,
        long targetValue,
        boolean completed
) {
}