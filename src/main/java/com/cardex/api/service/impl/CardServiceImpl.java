package com.cardex.api.service.impl;

import java.util.List;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardQuantityRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.CardNotFoundException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final PokemonTcgClient pokemonTcgClient;

    @Override
    @Transactional
    public CardResponse create(CreateCardRequest request) {
        return cardRepository
                .findByExternalIdAndLanguageAndCondition(
                        request.getExternalId(),
                        request.getLanguage(),
                        request.getCondition()
                )
                .map(existingCard -> increaseQuantity(existingCard, request))
                .orElseGet(() -> createNewCard(request));
    }

    private CardResponse increaseQuantity(
            CardEntity existingCard,
            CreateCardRequest request
    ) {
        int updatedQuantity =
                existingCard.getQuantity() + request.getQuantity();

        existingCard.setQuantity(updatedQuantity);

        CardEntity updatedCard = cardRepository.save(existingCard);

        return cardMapper.toResponse(updatedCard);
    }

    private CardResponse createNewCard(CreateCardRequest request) {
        PokemonCardApiSingleResponse apiResponse =
                pokemonTcgClient.findById(request.getExternalId());

        if (apiResponse == null || apiResponse.data() == null) {
            throw new PokemonCardNotFoundException(request.getExternalId());
        }

        PokemonCardApiData pokemonCard = apiResponse.data();

        CardEntity cardEntity = cardMapper.toEntity(request);

        cardEntity.setName(pokemonCard.name());
        cardEntity.setCollectionName(
                pokemonCard.set() != null
                        ? pokemonCard.set().name()
                        : null
        );
        cardEntity.setCardNumber(pokemonCard.number());
        cardEntity.setRarity(pokemonCard.rarity());
        cardEntity.setImageUrl(
                pokemonCard.images() != null
                        ? pokemonCard.images().large()
                        : null
        );

        CardEntity savedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> findAll() {
        Sort sort = Sort.by(
                Sort.Order.asc("name").ignoreCase()
        );

        return cardRepository.findAll(sort)
                .stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse findById(Long id) {
        CardEntity cardEntity = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        return cardMapper.toResponse(cardEntity);
    }

    @Override
    @Transactional
    public CardResponse update(Long id, UpdateCardRequest request) {
        CardEntity cardEntity = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardMapper.updateEntity(request, cardEntity);

        CardEntity updatedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(updatedCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CardEntity cardEntity = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.delete(cardEntity);
    }

    @Override
    @Transactional
    public CardResponse updateQuantity(
            Long id,
            UpdateCardQuantityRequest request
    ) {
        CardEntity cardEntity = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardEntity.setQuantity(request.quantity());

        CardEntity updatedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(updatedCard);
    }
}