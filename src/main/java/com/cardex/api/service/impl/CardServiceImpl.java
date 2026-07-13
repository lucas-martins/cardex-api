package com.cardex.api.service.impl;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final PokemonTcgClient pokemonTcgClient;

    @Override
    @Transactional
    public CardResponse create(CreateCardRequest request) {
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
}