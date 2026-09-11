package com.cardex.api.service;

import com.cardex.api.dto.response.CollectionChecklistResponse;
import com.cardex.api.dto.response.PublicShareSummaryResponse;
import com.cardex.api.dto.response.ShareStatusResponse;

public interface ShareService {

    ShareStatusResponse getStatus();

    ShareStatusResponse enable();

    void disable();

    PublicShareSummaryResponse getPublicSummary(String token);

    CollectionChecklistResponse getPublicChecklist(
            String token,
            String collectionId
    );
}
