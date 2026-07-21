package com.cardex.api.dto.response;

import java.util.List;

public record CardImportResponse(
        long totalRows,
        long importedRows,
        long skippedRows,
        List<CardImportPreviewItemResponse> skippedItems
) {
}