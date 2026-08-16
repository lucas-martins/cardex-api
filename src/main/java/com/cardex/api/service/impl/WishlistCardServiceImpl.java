package com.cardex.api.service.impl;

import com.cardex.api.dto.wishlist.UpdateWishlistPriorityRequest;
import com.cardex.api.dto.wishlist.WishlistCardRequest;
import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.entity.WishlistCardEntity;
import com.cardex.api.enumeration.WishlistPriority;
import com.cardex.api.exception.WishlistCardAlreadyExistsException;
import com.cardex.api.exception.WishlistCardNotFoundException;
import com.cardex.api.mapper.WishlistCardMapper;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.WishlistCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistCardServiceImpl
        implements WishlistCardService {

    private final WishlistCardRepository repository;
    private final WishlistCardMapper mapper;
    private final PokemonCardCatalogService
            pokemonCardCatalogService;
    private final AuthenticatedUserService
            authenticatedUserService;

    @Override
    public WishlistCardResponse create(
            WishlistCardRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService
                        .getAuthenticatedUser();

        if (repository.existsByUserAndExternalId(
                authenticatedUser,
                request.externalId()
        )) {
            throw new WishlistCardAlreadyExistsException(
                    request.externalId()
            );
        }

        PokemonCardCatalogEntity pokemonCard =
                pokemonCardCatalogService
                        .findByExternalId(
                                request.externalId()
                        );

        WishlistPriority priority =
                request.priority() != null
                        ? request.priority()
                        : WishlistPriority.MEDIUM;

        WishlistCardEntity entity =
                WishlistCardEntity.builder()
                        .user(authenticatedUser)
                        .externalId(
                                pokemonCard.getExternalId()
                        )
                        .name(
                                pokemonCard.getName()
                        )
                        .cardNumber(
                                pokemonCard.getCardNumber()
                        )
                        .collectionId(
                                pokemonCard.getCollectionId()
                        )
                        .collectionName(
                                pokemonCard.getCollectionName()
                        )
                        .series(
                                pokemonCard.getCollectionSeries()
                        )
                        .rarity(
                                pokemonCard.getRarity()
                        )
                        .imageUrl(
                                pokemonCard.getImageUrl()
                        )
                        .priority(priority)
                        .build();

        WishlistCardEntity savedEntity =
                repository.save(entity);

        return mapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistCardResponse> findAll() {
        UserEntity authenticatedUser =
                authenticatedUserService
                        .getAuthenticatedUser();

        return repository
                .findAllByUserOrderByCreatedAtDesc(
                        authenticatedUser
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService
                        .getAuthenticatedUser();

        WishlistCardEntity entity =
                repository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new WishlistCardNotFoundException(
                                                id
                                        )
                        );

        repository.delete(entity);
    }

    @Override
    public WishlistCardResponse updatePriority(
            Long id,
            UpdateWishlistPriorityRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        WishlistCardEntity entity =
                repository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new WishlistCardNotFoundException(
                                                id
                                        )
                        );

        entity.setPriority(
                request.priority()
        );

        WishlistCardEntity updatedEntity =
                repository.save(entity);

        return mapper.toResponse(updatedEntity);
    }
}