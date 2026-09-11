package com.cardex.api.service.impl;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardImagesApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.repository.PokemonCardCatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonCardCatalogServiceImplTest {

    @Mock
    private PokemonCardCatalogRepository
            pokemonCardCatalogRepository;

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @InjectMocks
    private PokemonCardCatalogServiceImpl
            pokemonCardCatalogService;

    @Test
    void shouldReturnCachedCollectionWithoutCallingPokemonApi() {
        PokemonCardCatalogEntity cachedCard =
                createCatalogCard(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common"
                );

        cachedCard.setPriceUpdatedAt(
                java.time.LocalDateTime.now()
        );

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("sm1")
        ).thenReturn(
                List.of(cachedCard)
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId("sm1");

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "sm1-1",
                result.get(0).getExternalId()
        );

        verify(
                pokemonCardCatalogRepository
        ).findByCollectionId("sm1");

        verifyNoInteractions(
                pokemonTcgClient
        );

        verify(
                pokemonCardCatalogRepository,
                never()
        ).saveAll(
                anyList()
        );
    }

    @Test
    void shouldLoadAndSaveCollectionWhenCacheIsEmpty() {
        PokemonSetApiData set =
                new PokemonSetApiData(
                        "sm1",
                        "Sun & Moon",
                        "Sun & Moon",
                        149,
                        173
                );

        PokemonCardApiData caterpie =
                new PokemonCardApiData(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common",
                        set,
                        new PokemonCardImagesApiData(
                                "caterpie-small",
                                "caterpie-large"
                        ),
                        null,
                        null
                );

        PokemonCardApiData metapod =
                new PokemonCardApiData(
                        "sm1-2",
                        "Metapod",
                        "2",
                        "Uncommon",
                        set,
                        new PokemonCardImagesApiData(
                                "metapod-small",
                                "metapod-large"
                        ),
                        null,
                        null
                );

        PokemonCardApiResponse response =
                new PokemonCardApiResponse(
                        List.of(
                                caterpie,
                                metapod
                        ),
                        1,
                        250,
                        2,
                        2
                );

        PokemonCardCatalogEntity caterpieEntity =
                toCatalogCard(caterpie);

        PokemonCardCatalogEntity metapodEntity =
                toCatalogCard(metapod);

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("sm1")
        ).thenReturn(
                List.of(),
                List.of(
                        caterpieEntity,
                        metapodEntity
                )
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "sm1",
                        1,
                        250
                )
        ).thenReturn(
                response
        );

        when(
                pokemonCardCatalogRepository
                        .saveAll(anyList())
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId("sm1");

        assertEquals(
                2,
                result.size()
        );

        PokemonCardCatalogEntity first =
                result.get(0);

        assertEquals(
                "sm1-1",
                first.getExternalId()
        );

        assertEquals(
                "Caterpie",
                first.getName()
        );

        assertEquals(
                "sm1",
                first.getCollectionId()
        );

        assertEquals(
                "Sun & Moon",
                first.getCollectionName()
        );

        assertEquals(
                "Sun & Moon",
                first.getCollectionSeries()
        );

        assertEquals(
                173,
                first.getCollectionTotal()
        );

        assertEquals(
                "caterpie-large",
                first.getImageUrl()
        );

        verify(
                pokemonTcgClient
        ).searchBySetId(
                "sm1",
                1,
                250
        );

        verify(
                pokemonCardCatalogRepository
        ).saveAll(
                argThat(cards -> {
                    int count = 0;

                    for (PokemonCardCatalogEntity ignored : cards) {
                        count++;
                    }

                    return count == 2;
                })
        );

        verify(
                pokemonCardCatalogRepository,
                times(2)
        ).findByCollectionId("sm1");
    }

    @Test
    void shouldLoadAllPagesBeforeSavingCollection() {
        PokemonSetApiData set =
                new PokemonSetApiData(
                        "test1",
                        "Test Collection",
                        "Test Series",
                        300,
                        300
                );

        PokemonCardApiData firstCard =
                new PokemonCardApiData(
                        "test1-1",
                        "First Card",
                        "1",
                        "Common",
                        set,
                        null,
                        null,
                        null
                );

        PokemonCardApiData secondCard =
                new PokemonCardApiData(
                        "test1-251",
                        "Second Card",
                        "251",
                        "Rare",
                        set,
                        null,
                        null,
                        null
                );

        PokemonCardApiResponse firstPage =
                new PokemonCardApiResponse(
                        List.of(firstCard),
                        1,
                        250,
                        1,
                        2
                );

        PokemonCardApiResponse secondPage =
                new PokemonCardApiResponse(
                        List.of(secondCard),
                        2,
                        250,
                        1,
                        2
                );

        PokemonCardCatalogEntity firstCardEntity =
                toCatalogCard(firstCard);

        PokemonCardCatalogEntity secondCardEntity =
                toCatalogCard(secondCard);

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("test1")
        ).thenReturn(
                List.of(),
                List.of(
                        firstCardEntity,
                        secondCardEntity
                )
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "test1",
                        1,
                        250
                )
        ).thenReturn(
                firstPage
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "test1",
                        2,
                        250
                )
        ).thenReturn(
                secondPage
        );

        when(
                pokemonCardCatalogRepository
                        .saveAll(anyList())
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId("test1");

        assertEquals(
                2,
                result.size()
        );

        verify(
                pokemonTcgClient
        ).searchBySetId(
                "test1",
                1,
                250
        );

        verify(
                pokemonTcgClient
        ).searchBySetId(
                "test1",
                2,
                250
        );

        verify(
                pokemonCardCatalogRepository
        ).saveAll(
                argThat(cards -> {
                    int count = 0;

                    for (PokemonCardCatalogEntity ignored : cards) {
                        count++;
                    }

                    return count == 2;
                })
        );
    }

    @Test
    void shouldReturnEmptyListWhenPokemonApiReturnsNoCards() {
        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("invalid")
        ).thenReturn(
                List.of(),
                List.of()
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "invalid",
                        1,
                        250
                )
        ).thenReturn(
                new PokemonCardApiResponse(
                        List.of(),
                        1,
                        250,
                        0,
                        0
                )
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId("invalid");

        assertTrue(
                result.isEmpty()
        );

        verify(
                pokemonCardCatalogRepository,
                never()
        ).saveAll(
                anyList()
        );
    }

    @Test
    void shouldCompleteCollectionWhenCacheIsPartial() {
        PokemonCardCatalogEntity cachedCard =
                new PokemonCardCatalogEntity();

        cachedCard.setExternalId(
                "sv3pt5-1"
        );

        cachedCard.setCollectionId(
                "sv3pt5"
        );

        cachedCard.setCollectionName(
                "151"
        );

        cachedCard.setCollectionSeries(
                "Scarlet & Violet"
        );

        cachedCard.setCollectionTotal(
                3
        );

        PokemonCardApiData firstCard =
                createPokemonCard(
                        "sv3pt5-1",
                        "Bulbasaur"
                );

        PokemonCardApiData secondCard =
                createPokemonCard(
                        "sv3pt5-2",
                        "Ivysaur"
                );

        PokemonCardApiData thirdCard =
                createPokemonCard(
                        "sv3pt5-3",
                        "Venusaur ex"
                );

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("sv3pt5")
        ).thenReturn(
                List.of(cachedCard),
                List.of(
                        cachedCard,
                        toCatalogCard(
                                secondCard
                        ),
                        toCatalogCard(
                                thirdCard
                        )
                )
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "sv3pt5",
                        1,
                        250
                )
        ).thenReturn(
                new PokemonCardApiResponse(
                        List.of(
                                firstCard,
                                secondCard,
                                thirdCard
                        ),
                        1,
                        250,
                        3,
                        3
                )
        );

        when(
                pokemonCardCatalogRepository
                        .saveAll(anyList())
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId(
                                "sv3pt5"
                        );

        assertEquals(
                3,
                result.size()
        );

        verify(
                pokemonTcgClient
        ).searchBySetId(
                "sv3pt5",
                1,
                250
        );

        verify(
                pokemonCardCatalogRepository
        ).saveAll(
                argThat(cards -> {
                    int count = 0;

                    for (PokemonCardCatalogEntity ignored : cards) {
                        count++;
                    }

                    return count == 2;
                })
        );
    }

    @Test
    void shouldReturnCachedCollectionWhenCacheIsComplete() {
        PokemonCardCatalogEntity firstCard =
                new PokemonCardCatalogEntity();

        firstCard.setExternalId(
                "base1-1"
        );

        firstCard.setCollectionId(
                "base1"
        );

        firstCard.setCollectionTotal(
                2
        );

        PokemonCardCatalogEntity secondCard =
                new PokemonCardCatalogEntity();

        secondCard.setExternalId(
                "base1-2"
        );

        secondCard.setCollectionId(
                "base1"
        );

        secondCard.setCollectionTotal(
                2
        );

        firstCard.setPriceUpdatedAt(
                java.time.LocalDateTime.now()
        );

        secondCard.setPriceUpdatedAt(
                java.time.LocalDateTime.now()
        );

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("base1")
        ).thenReturn(
                List.of(
                        firstCard,
                        secondCard
                )
        );

        List<PokemonCardCatalogEntity> result =
                pokemonCardCatalogService
                        .findByCollectionId(
                                "base1"
                        );

        assertEquals(
                2,
                result.size()
        );

        verifyNoInteractions(
                pokemonTcgClient
        );

        verify(
                pokemonCardCatalogRepository,
                never()
        ).saveAll(
                anyList()
        );
    }

    private PokemonCardCatalogEntity createCatalogCard(
            String externalId,
            String name,
            String cardNumber,
            String rarity
    ) {
        PokemonCardCatalogEntity card =
                new PokemonCardCatalogEntity();

        card.setExternalId(
                externalId
        );

        card.setName(
                name
        );

        card.setCardNumber(
                cardNumber
        );

        card.setRarity(
                rarity
        );

        card.setCollectionId(
                "sm1"
        );

        card.setCollectionName(
                "Sun & Moon"
        );

        card.setCollectionSeries(
                "Sun & Moon"
        );

        card.setCollectionTotal(
                1
        );

        return card;
    }

    private PokemonCardApiData createPokemonCard(
            String externalId,
            String name
    ) {
        PokemonSetApiData set =
                new PokemonSetApiData(
                        "sv3pt5",
                        "151",
                        "Scarlet & Violet",
                        3,
                        3
                );

        return new PokemonCardApiData(
                externalId,
                name,
                externalId.substring(
                        externalId.lastIndexOf("-") + 1
                ),
                "Common",
                set,
                new PokemonCardImagesApiData(
                        externalId + "-small",
                        externalId + "-large"
                ),
                null,
                null
        );
    }

    private PokemonCardCatalogEntity toCatalogCard(
            PokemonCardApiData card
    ) {
        PokemonCardCatalogEntity entity =
                new PokemonCardCatalogEntity();

        entity.setExternalId(
                card.id()
        );

        entity.setName(
                card.name()
        );

        entity.setCardNumber(
                card.number()
        );

        entity.setRarity(
                card.rarity()
        );

        if (card.set() != null) {
            entity.setCollectionId(
                    card.set().id()
            );

            entity.setCollectionName(
                    card.set().name()
            );

            entity.setCollectionSeries(
                    card.set().series()
            );

            entity.setCollectionTotal(
                    card.set().total()
            );
        }

        if (card.images() != null) {
            entity.setImageUrl(
                    card.images().large()
            );
        }

        return entity;
    }
}