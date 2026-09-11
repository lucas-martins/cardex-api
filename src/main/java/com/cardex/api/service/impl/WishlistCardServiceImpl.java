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
import com.cardex.api.service.ExchangeRateService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.WishlistCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cardex.api.pokemon.PokemonCardPriceExtractor;

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
    private final ExchangeRateService exchangeRateService;

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

        return PokemonCardPriceExtractor.enrich(
                mapper.toResponse(savedEntity),
                pokemonCard,
                exchangeRateService::toBrl
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistCardResponse> findAll() {
        UserEntity authenticatedUser =
                authenticatedUserService
                        .getAuthenticatedUser();

        List<WishlistCardEntity> wishlistCards =
                repository.findAllByUserOrderByCreatedAtDesc(
                        authenticatedUser
                );

        return enrichAll(wishlistCards);
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

        return enrich(updatedEntity);
    }

    private List<WishlistCardResponse> enrichAll(
            List<WishlistCardEntity> wishlistCards
    ) {
        List<String> externalIds =
                wishlistCards
                        .stream()
                        .map(WishlistCardEntity::getExternalId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList();

        Map<String, PokemonCardCatalogEntity> catalogByExternalId =
                Optional.ofNullable(
                                pokemonCardCatalogService
                                        .findAllByExternalIdIn(externalIds)
                        )
                        .orElse(List.of())
                        .stream()
                        .collect(Collectors.toMap(
                                PokemonCardCatalogEntity::getExternalId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        return wishlistCards
                .stream()
                .map(card ->
                        PokemonCardPriceExtractor.enrich(
                                mapper.toResponse(card),
                                catalogByExternalId.get(
                                        card.getExternalId()
                                ),
                                exchangeRateService::toBrl
                        )
                )
                .toList();
    }

    private WishlistCardResponse enrich(
            WishlistCardEntity entity
    ) {
        return enrichAll(List.of(entity)).get(0);
    }
}