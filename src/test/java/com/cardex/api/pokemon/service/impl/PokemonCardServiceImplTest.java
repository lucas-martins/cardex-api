package com.cardex.api.pokemon.service.impl;

import com.cardex.api.entity.PokemonSetCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.mapper.PokemonCardMapper;
import com.cardex.api.pokemon.response.PokemonCollectionResponse;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.repository.projection.CollectionOwnedCardsProjection;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.PokemonSetCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonCardServiceImplTest {

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @Mock
    private PokemonCardMapper pokemonCardMapper;

    @Mock
    private PokemonCardCatalogService
            pokemonCardCatalogService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private WishlistCardRepository
            wishlistCardRepository;

    @Mock
    private AuthenticatedUserService
            authenticatedUserService;

    @Mock
    private PokemonSetCatalogService
            pokemonSetCatalogService;

    private PokemonCardServiceImpl service;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        service =
                new PokemonCardServiceImpl(
                        pokemonTcgClient,
                        pokemonCardMapper,
                        pokemonCardCatalogService,
                        cardRepository,
                        wishlistCardRepository,
                        authenticatedUserService,
                        pokemonSetCatalogService
                );

        user = new UserEntity();
    }

    @Test
    void shouldReturnCollectionsWithUserProgress() {
        PokemonSetCatalogEntity base =
                createSet(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102
                );

        PokemonSetCatalogEntity jungle =
                createSet(
                        "base2",
                        "Jungle",
                        "Base",
                        64,
                        64
                );

        CollectionOwnedCardsProjection baseProgress =
                mock(
                        CollectionOwnedCardsProjection.class
                );

        when(baseProgress.getCollectionId())
                .thenReturn("base1");

        when(baseProgress.getOwnedCards())
                .thenReturn(12L);

        when(authenticatedUserService
                .getAuthenticatedUser())
                .thenReturn(user);

        when(pokemonSetCatalogService.findAll())
                .thenReturn(
                        List.of(
                                base,
                                jungle
                        )
                );

        when(cardRepository
                .findOwnedCardsGroupedByCollection(
                        user
                ))
                .thenReturn(
                        List.of(baseProgress)
                );

        List<PokemonCollectionResponse> result =
                service.findCollections();

        assertEquals(
                2,
                result.size()
        );

        PokemonCollectionResponse baseResult =
                result.get(0);

        assertEquals(
                "base1",
                baseResult.id()
        );

        assertEquals(
                "Base",
                baseResult.name()
        );

        assertEquals(
                "Base",
                baseResult.series()
        );

        assertEquals(
                102,
                baseResult.printedTotal()
        );

        assertEquals(
                102,
                baseResult.total()
        );

        assertEquals(
                12,
                baseResult.ownedCards()
        );

        assertEquals(
                11.76,
                baseResult.completionPercentage()
        );

        PokemonCollectionResponse jungleResult =
                result.get(1);

        assertEquals(
                "base2",
                jungleResult.id()
        );

        assertEquals(
                0,
                jungleResult.ownedCards()
        );

        assertEquals(
                0.0,
                jungleResult.completionPercentage()
        );

        verify(pokemonSetCatalogService)
                .findAll();

        verify(cardRepository)
                .findOwnedCardsGroupedByCollection(
                        user
                );

        verify(
                pokemonTcgClient,
                never()
        ).findSets();
    }

    @Test
    void shouldReturnEmptyListWhenCatalogReturnsNoCollections() {
        when(authenticatedUserService
                .getAuthenticatedUser())
                .thenReturn(user);

        when(pokemonSetCatalogService.findAll())
                .thenReturn(
                        List.of()
                );

        List<PokemonCollectionResponse> result =
                service.findCollections();

        assertEquals(
                List.of(),
                result
        );

        verify(pokemonSetCatalogService)
                .findAll();

        verify(
                cardRepository,
                never()
        ).findOwnedCardsGroupedByCollection(
                any()
        );

        verify(
                pokemonTcgClient,
                never()
        ).findSets();
    }

    @Test
    void shouldReturnZeroPercentageWhenPrintedTotalIsZero() {
        PokemonSetCatalogEntity collection =
                createSet(
                        "test",
                        "Test Set",
                        "Test",
                        0,
                        0
                );

        CollectionOwnedCardsProjection progress =
                mock(
                        CollectionOwnedCardsProjection.class
                );

        when(progress.getCollectionId())
                .thenReturn("test");

        when(progress.getOwnedCards())
                .thenReturn(5L);

        when(authenticatedUserService
                .getAuthenticatedUser())
                .thenReturn(user);

        when(pokemonSetCatalogService.findAll())
                .thenReturn(
                        List.of(collection)
                );

        when(cardRepository
                .findOwnedCardsGroupedByCollection(
                        user
                ))
                .thenReturn(
                        List.of(progress)
                );

        List<PokemonCollectionResponse> result =
                service.findCollections();

        assertEquals(
                0.0,
                result
                        .get(0)
                        .completionPercentage()
        );
    }

    @Test
    void shouldReturnZeroProgressWhenUserOwnsNoCardsFromCollection() {
        PokemonSetCatalogEntity collection =
                createSet(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102
                );

        when(authenticatedUserService
                .getAuthenticatedUser())
                .thenReturn(user);

        when(pokemonSetCatalogService.findAll())
                .thenReturn(
                        List.of(collection)
                );

        when(cardRepository
                .findOwnedCardsGroupedByCollection(
                        user
                ))
                .thenReturn(
                        List.of()
                );

        List<PokemonCollectionResponse> result =
                service.findCollections();

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                0,
                result.get(0).ownedCards()
        );

        assertEquals(
                0.0,
                result.get(0).completionPercentage()
        );
    }

    private PokemonSetCatalogEntity createSet(
            String collectionId,
            String name,
            String series,
            Integer printedTotal,
            Integer total
    ) {
        PokemonSetCatalogEntity entity =
                new PokemonSetCatalogEntity();

        entity.setCollectionId(
                collectionId
        );

        entity.setName(
                name
        );

        entity.setSeries(
                series
        );

        entity.setPrintedTotal(
                printedTotal
        );

        entity.setTotal(
                total
        );

        entity.setLastSyncedAt(
                LocalDateTime.now()
        );

        return entity;
    }
}