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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonCardCatalogServiceImplTest {

    @Mock
    private PokemonCardCatalogRepository pokemonCardCatalogRepository;

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @InjectMocks
    private PokemonCardCatalogServiceImpl pokemonCardCatalogService;

    @Test
    void shouldReturnCachedCollectionWithoutCallingPokemonApi() {
        PokemonCardCatalogEntity cachedCard =
                createCatalogCard(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common"
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

        assertEquals(1, result.size());
        assertEquals(
                "sm1-1",
                result.get(0).getExternalId()
        );

        verify(
                pokemonCardCatalogRepository
        ).findByCollectionId("sm1");

        verifyNoInteractions(pokemonTcgClient);

        verify(
                pokemonCardCatalogRepository,
                never()
        ).saveAll(anyList());
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
                        )
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
                        )
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

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("sm1")
        ).thenReturn(
                List.of()
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "sm1",
                        1,
                        250
                )
        ).thenReturn(response);

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

        assertEquals(2, result.size());

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
        ).saveAll(anyList());
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
                        null
                );

        PokemonCardApiData secondCard =
                new PokemonCardApiData(
                        "test1-251",
                        "Second Card",
                        "251",
                        "Rare",
                        set,
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

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("test1")
        ).thenReturn(
                List.of()
        );

        when(
                pokemonTcgClient.searchBySetId(
                        "test1",
                        1,
                        250
                )
        ).thenReturn(firstPage);

        when(
                pokemonTcgClient.searchBySetId(
                        "test1",
                        2,
                        250
                )
        ).thenReturn(secondPage);

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

        assertEquals(2, result.size());

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

        assertTrue(result.isEmpty());

        verify(
                pokemonCardCatalogRepository
        ).saveAll(
                List.of()
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

        card.setExternalId(externalId);
        card.setName(name);
        card.setCardNumber(cardNumber);
        card.setRarity(rarity);
        card.setCollectionId("sm1");
        card.setCollectionName("Sun & Moon");

        return card;
    }
}