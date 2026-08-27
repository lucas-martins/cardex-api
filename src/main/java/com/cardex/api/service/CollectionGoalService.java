package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCollectionGoalRequest;
import com.cardex.api.dto.request.UpdateCollectionGoalRequest;
import com.cardex.api.dto.response.UserCollectionGoalResponse;

import java.util.List;

public interface CollectionGoalService {

    UserCollectionGoalResponse create(
            CreateCollectionGoalRequest request
    );

    List<UserCollectionGoalResponse> findAll();

    UserCollectionGoalResponse update(
            Long id,
            UpdateCollectionGoalRequest request
    );

    void delete(Long id);
}