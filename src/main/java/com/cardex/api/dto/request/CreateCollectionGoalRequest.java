package com.cardex.api.dto.request;

import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.enumeration.CollectionGoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCollectionGoalRequest(

        @NotBlank(message = "Title is required")
        @Size(
                max = 150,
                message = "Title must not exceed 150 characters"
        )
        String title,

        @NotNull(message = "Goal type is required")
        CollectionGoalType type,

        Integer targetValue,

        String collectionId,

        CardLanguage language
) {
}