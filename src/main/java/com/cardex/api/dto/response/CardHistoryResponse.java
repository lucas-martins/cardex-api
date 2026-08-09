package com.cardex.api.dto.response;

import com.cardex.api.enumeration.CardHistoryAction;

import java.time.LocalDateTime;

public record CardHistoryResponse(
        Long id,
        Long cardId,
        String externalId,
        String cardName,
        CardHistoryAction action,
        String description,
        LocalDateTime createdAt
) {
}