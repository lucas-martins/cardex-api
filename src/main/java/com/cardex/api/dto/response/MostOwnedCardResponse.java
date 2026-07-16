package com.cardex.api.dto.response;

public record MostOwnedCardResponse(
        String name,
        int quantity
) {
}