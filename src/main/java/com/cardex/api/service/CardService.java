package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardFavoriteRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.*;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import org.springframework.data.domain.Page;

import java.util.List;

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

    CollectionGoalsResponse getCollectionGoals();

    List<CollectionProgressResponse> getCollectionProgress();

    RefreshCardMetadataResponse refreshMetadata();

    CollectionDetailsResponse getCollectionDetails(String collectionId);
}