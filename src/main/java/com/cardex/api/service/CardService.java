package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardFavoriteRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.dto.response.CollectionAnalyticsResponse;
import com.cardex.api.dto.response.CollectionSummaryResponse;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import org.springframework.data.domain.Page;

public interface CardService {

    CardResponse create(CreateCardRequest request);

    Page<CardResponse> findAll(
            int page,
            int size,
            String name,
            CardLanguage language,
            CardCondition condition,
            Boolean favorite,
            String sort
    );

    CardResponse findById(Long id);

    CardResponse update(Long id, UpdateCardRequest request);

    void delete(Long id);

    CollectionSummaryResponse getCollectionSummary();

    CardResponse updateFavorite(
            Long id,
            UpdateCardFavoriteRequest request
    );

    CollectionAnalyticsResponse getCollectionAnalytics();
}