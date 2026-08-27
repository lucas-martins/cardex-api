package com.cardex.api.dto.response;

import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.enumeration.CollectionGoalType;

import java.time.LocalDateTime;

public record UserCollectionGoalResponse(

        Long id,

        String title,

        CollectionGoalType type,

        Integer targetValue,

        String collectionId,

        String collectionName,

        CardLanguage language,

        int currentValue,

        int goalValue,

        double completionPercentage,

        boolean completed,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}