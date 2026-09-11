package com.cardex.api.pokemon.service.impl;

import com.cardex.api.entity.*;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.PokemonCardPriceExtractor;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonSetApiResponse;
import com.cardex.api.pokemon.mapper.PokemonCardMapper;
import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import com.cardex.api.pokemon.response.PokemonCollectionResponse;
import com.cardex.api.pokemon.service.PokemonCardService;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.repository.projection.CollectionOwnedCardsProjection;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.ExchangeRateService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.PokemonSetCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokemonCardServiceImpl
        implements PokemonCardService {

    private final PokemonTcgClient pokemonTcgClient;
    private final PokemonCardMapper pokemonCardMapper;
    private final PokemonCardCatalogService pokemonCardCatalogService;
    private final CardRepository cardRepository;
    private final WishlistCardRepository wishlistCardRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final PokemonSetCatalogService pokemonSetCatalogService;
    private final ExchangeRateService exchangeRateService;

    @Override
    public PokemonCardSearchPageResponse search(
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int size
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        try {
            PokemonCardApiResponse apiResponse =
                    pokemonTcgClient.searchCards(
                            name,
                            setId,
                            number,
                            rarity,
                            page,
                            size
                    );

            if (apiResponse == null
                    || apiResponse.data() == null) {
                return emptyResponse(
                        page,
                        size
                );
            }

            pokemonCardCatalogService.cacheCards(
                    apiResponse.data()
            );

            List<PokemonCardSearchResponse> baseCards =
                    apiResponse.data()
                            .stream()
                            .map(card -> {
                                BigDecimal marketPriceUsd =
                                        PokemonCardPriceExtractor.usdFrom(
                                                card
                                        );

                                BigDecimal marketPriceEur =
                                        PokemonCardPriceExtractor.eurFrom(
                                                card
                                        );

                                return PokemonCardPriceExtractor.enrich(
                                        pokemonCardMapper.toSearchResponse(
                                                card
                                        ),
                                        marketPriceUsd,
                                        marketPriceEur,
                                        exchangeRateService.toBrl(
                                                marketPriceUsd,
                                                marketPriceEur
                                        )
                                );
                            })
                            .toList();

            List<PokemonCardSearchResponse> cards =
                    enrichWithUserState(
                            baseCards,
                            authenticatedUser
                    );

            int totalElements =
                    apiResponse.totalCount() != null
                            ? apiResponse.totalCount()
                            : 0;

            int totalPages =
                    size > 0
                            ? (int) Math.ceil(
                            (double) totalElements
                            / size
                    )
                            : 0;

            return new PokemonCardSearchPageResponse(
                    cards,
                    apiResponse.page(),
                    apiResponse.pageSize(),
                    apiResponse.count(),
                    totalElements,
                    totalPages,
                    apiResponse.page() == 1,
                    totalPages == 0
                            || apiResponse.page()
                            >= totalPages
            );
        } catch (
                PokemonTcgApiUnavailableException exception
        ) {
            return searchFromLocalCatalog(
                    name,
                    setId,
                    number,
                    rarity,
                    page,
                    size,
                    authenticatedUser,
                    exception
            );
        }
    }

    @Override
    public List<PokemonCollectionResponse> findCollections() {
        UserEntity authenticatedUser =
                authenticatedUserService
                        .getAuthenticatedUser();

        List<PokemonSetCatalogEntity> sets =
                pokemonSetCatalogService.findAll();

        if (sets.isEmpty()) {
            return List.of();
        }

        Map<String, Long> ownedCardsByCollection =
                cardRepository
                        .findOwnedCardsGroupedByCollection(
                                authenticatedUser
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        CollectionOwnedCardsProjection
                                                ::getCollectionId,
                                        CollectionOwnedCardsProjection
                                                ::getOwnedCards
                                )
                        );

        return sets
                .stream()
                .map(set -> {
                    long ownedCards =
                            ownedCardsByCollection
                                    .getOrDefault(
                                            set.getCollectionId(),
                                            0L
                                    );

                    double completionPercentage =
                            calculateCompletionPercentage(
                                    ownedCards,
                                    set.getTotal()
                            );

                    return new PokemonCollectionResponse(
                            set.getCollectionId(),
                            set.getName(),
                            set.getSeries(),
                            set.getPrintedTotal(),
                            set.getTotal(),
                            ownedCards,
                            completionPercentage
                    );
                })
                .toList();
    }

    private double calculateCompletionPercentage(
            long ownedCards,
            Integer totalCards
    ) {
        if (totalCards == null
                || totalCards <= 0) {
            return 0.0;
        }

        double percentage =
                (double) ownedCards
                        / totalCards
                        * 100;

        return Math.round(
                percentage * 100.0
        ) / 100.0;
    }

    private PokemonCardSearchPageResponse searchFromLocalCatalog(
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int size,
            UserEntity authenticatedUser,
            PokemonTcgApiUnavailableException exception
    ) {
        Page<PokemonCardCatalogEntity> localPage =
                pokemonCardCatalogService.search(
                        name,
                        setId,
                        number,
                        rarity,
                        page,
                        size
                );

        if (localPage.isEmpty()) {
            throw exception;
        }

        List<PokemonCardSearchResponse> baseCards =
                localPage
                        .getContent()
                        .stream()
                        .map(catalogCard ->
                                PokemonCardPriceExtractor.enrich(
                                        pokemonCardMapper.toSearchResponse(
                                                catalogCard
                                        ),
                                        catalogCard.getMarketPriceUsd(),
                                        catalogCard.getMarketPriceEur(),
                                        exchangeRateService.toBrl(
                                                catalogCard.getMarketPriceUsd(),
                                                catalogCard.getMarketPriceEur()
                                        )
                                )
                        )
                        .toList();

        List<PokemonCardSearchResponse> cards =
                enrichWithUserState(
                        baseCards,
                        authenticatedUser
                );

        return new PokemonCardSearchPageResponse(
                cards,
                page,
                size,
                cards.size(),
                (int) localPage.getTotalElements(),
                localPage.getTotalPages(),
                localPage.isFirst(),
                localPage.isLast()
        );
    }

    private List<PokemonCardSearchResponse> enrichWithUserState(
            List<PokemonCardSearchResponse> cards,
            UserEntity authenticatedUser
    ) {
        if (cards.isEmpty()) {
            return cards;
        }

        List<String> externalIds =
                cards
                        .stream()
                        .map(
                                PokemonCardSearchResponse::externalId
                        )
                        .distinct()
                        .toList();

        Map<String, CardEntity> ownedCardsByExternalId =
                cardRepository
                        .findByUserAndExternalIdIn(
                                authenticatedUser,
                                externalIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        CardEntity::getExternalId,
                                        Function.identity(),
                                        (first, second) -> first
                                )
                        );

        Map<String, WishlistCardEntity> wishlistCardsByExternalId =
                wishlistCardRepository
                        .findByUserAndExternalIdIn(
                                authenticatedUser,
                                externalIds
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        WishlistCardEntity::getExternalId,
                                        Function.identity(),
                                        (first, second) -> first
                                )
                        );

        return cards
                .stream()
                .map(card -> {
                    CardEntity ownedCard =
                            ownedCardsByExternalId.get(
                                    card.externalId()
                            );

                    WishlistCardEntity wishlistCard =
                            wishlistCardsByExternalId.get(
                                    card.externalId()
                            );

                    return new PokemonCardSearchResponse(
                            card.externalId(),
                            card.name(),
                            card.collectionName(),
                            card.cardNumber(),
                            card.rarity(),
                            card.imageUrl(),
                            ownedCard != null,
                            ownedCard != null
                                    ? ownedCard.getId()
                                    : null,
                            wishlistCard != null,
                            wishlistCard != null
                                    ? wishlistCard.getId()
                                    : null,
                            wishlistCard != null
                                    ? wishlistCard.getPriority()
                                    : null,
                            card.marketPriceUsd(),
                            card.marketPriceEur(),
                            card.marketPriceBrl()
                    );
                })
                .toList();
    }

    private PokemonCardSearchPageResponse emptyResponse(
            int page,
            int size
    ) {
        return new PokemonCardSearchPageResponse(
                List.of(),
                page,
                size,
                0,
                0,
                0,
                page == 1,
                true
        );
    }
}