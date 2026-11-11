package com.cardex.api.service;

import com.cardex.api.dto.deck.AddDeckCardRequest;
import com.cardex.api.dto.deck.CreateDeckRequest;
import com.cardex.api.dto.deck.DeckParseResponse;
import com.cardex.api.dto.deck.DeckResponse;
import com.cardex.api.dto.deck.DeckSummaryResponse;
import com.cardex.api.dto.deck.ParseDeckListRequest;
import com.cardex.api.dto.deck.ReplaceDeckCardsRequest;
import com.cardex.api.dto.deck.UpdateDeckRequest;

import java.util.List;

public interface DeckService {

    DeckParseResponse parse(ParseDeckListRequest request);

    List<DeckSummaryResponse> findAll();

    DeckResponse findById(Long id);

    DeckResponse create(CreateDeckRequest request);

    DeckResponse update(Long id, UpdateDeckRequest request);

    DeckResponse replaceCards(Long id, ReplaceDeckCardsRequest request);

    DeckResponse addCard(Long id, AddDeckCardRequest request);

    DeckResponse removeCard(Long deckId, Long cardId);

    void delete(Long id);
}
