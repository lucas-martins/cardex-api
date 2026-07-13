package com.cardex.api.service;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;

public interface CardService {

    CardResponse create(CreateCardRequest request);
}