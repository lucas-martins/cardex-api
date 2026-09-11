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
            String number,
            String collection,
            String rarity,
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

    List<CollectionProgressResponse> getCollectionProgressForUser(
            com.cardex.api.entity.UserEntity user
    );

    RefreshCardMetadataResponse refreshMetadata();

    CollectionDetailsResponse getCollectionDetails(String collectionId);

    CollectionChecklistResponse getCollectionChecklist(
            String collectionId
    );

    CollectionChecklistResponse getCollectionChecklistForUser(
            com.cardex.api.entity.UserEntity user,
            String collectionId
    );
}