package com.cardex.api.dto.response;

public record CardImportPreviewItemResponse(
        long line,
        String externalId,
        String name,
        String collectionName,
        String cardNumber,
        int quantity,
        boolean valid,
        String error
) {
}