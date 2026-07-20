package com.cardex.api.dto.response;

import java.util.List;

public record CardImportPreviewResponse(
        long totalRows,
        long validRows,
        long invalidRows,
        List<CardImportPreviewItemResponse> items
) {
}