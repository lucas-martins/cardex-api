package com.cardex.api.dto.response;

import java.util.List;

public record CollectionGoalsResponse(
        long completedGoals,
        long totalGoals,
        List<CollectionGoalResponse> goals
) {
}