package com.cardex.api.service.impl;

import com.cardex.api.exception.WishlistCardNotFoundException;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.dto.wishlist.WishlistCardRequest;
import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.WishlistCardEntity;
import com.cardex.api.exception.WishlistCardAlreadyExistsException;
import com.cardex.api.mapper.WishlistCardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.service.WishlistCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistCardServiceImpl implements WishlistCardService {

    private final WishlistCardRepository repository;
    private final WishlistCardMapper mapper;
    private final PokemonTcgClient pokemonTcgClient;

    @Override
    public WishlistCardResponse create(WishlistCardRequest request) {

        if (repository.existsByExternalId(request.externalId())) {
            throw new WishlistCardAlreadyExistsException(request.externalId());
        }

        PokemonCardApiSingleResponse response =
                pokemonTcgClient.findById(request.externalId());

        var pokemonCard = response.data();

        WishlistCardEntity entity =
                WishlistCardEntity.builder()
                        .externalId(pokemonCard.id())
                        .name(pokemonCard.name())
                        .cardNumber(pokemonCard.number())
                        .collectionId(pokemonCard.set().id())
                        .collectionName(pokemonCard.set().name())
                        .series(pokemonCard.set().series())
                        .rarity(pokemonCard.rarity())
                        .imageUrl(pokemonCard.images().large())
                        .build();

        repository.save(entity);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistCardResponse> findAll() {
        return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        WishlistCardEntity entity = repository
                .findById(id)
                .orElseThrow(() -> new WishlistCardNotFoundException(id));

        repository.delete(entity);
    }
}