package com.cardex.api.service;

import com.cardex.api.dto.response.CardHistoryResponse;
import com.cardex.api.enumeration.CardHistoryAction;
import org.springframework.data.domain.Page;

public interface CardHistoryService {

    Page<CardHistoryResponse> findAll(
            int page,
            int size,
            CardHistoryAction action
    );
}