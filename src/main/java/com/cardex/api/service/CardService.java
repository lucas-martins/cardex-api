package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;

import java.util.List;

public interface CardService {

    CardResponse create(CreateCardRequest request);

    List<CardResponse> findAll();
}