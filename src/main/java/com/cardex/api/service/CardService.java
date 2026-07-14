package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.CardResponse;

import java.util.List;

public interface CardService {

    CardResponse create(CreateCardRequest request);

    List<CardResponse> findAll();

    CardResponse findById(Long id);

    CardResponse update(Long id, UpdateCardRequest request);

    void delete(Long id);
}