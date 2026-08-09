package com.cardex.api.service;

import com.cardex.api.dto.response.CardHistoryResponse;
import org.springframework.data.domain.Page;

public interface CardHistoryService {

    Page<CardHistoryResponse> findAll(
            int page,
            int size
    );
}