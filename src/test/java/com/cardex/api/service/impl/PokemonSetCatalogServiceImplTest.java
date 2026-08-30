package com.cardex.api.service.impl;

import com.cardex.api.entity.PokemonSetCatalogEntity;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiResponse;
import com.cardex.api.repository.PokemonSetCatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonSetCatalogServiceImplTest {

    @Mock
    private PokemonSetCatalogRepository
            pokemonSetCatalogRepository;

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @InjectMocks
    private PokemonSetCatalogServiceImpl
            pokemonSetCatalogService;

    @Test
    void shouldReturnFreshCacheWithoutCallingPokemonApi() {
        PokemonSetCatalogEntity cachedSet =
                createCachedSet(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102,
                        LocalDateTime.now()
                );

        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of(cachedSet)
                );

        List<PokemonSetCatalogEntity> result =
                pokemonSetCatalogService.findAll();

        assertEquals(1, result.size());

        assertEquals(
                "base1",
                result.get(0).getCollectionId()
        );

        verifyNoInteractions(
                pokemonTcgClient
        );

        verify(
                pokemonSetCatalogRepository,
                never()
        ).saveAll(anyList());
    }

    @Test
    void shouldLoadAndSaveSetsWhenCacheIsEmpty() {
        PokemonSetApiData base =
                new PokemonSetApiData(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102
                );

        PokemonSetApiData jungle =
                new PokemonSetApiData(
                        "base2",
                        "Jungle",
                        "Base",
                        64,
                        64
                );

        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of()
                );

        when(pokemonTcgClient.findSets())
                .thenReturn(
                        new PokemonSetApiResponse(
                                List.of(
                                        jungle,
                                        base
                                )
                        )
                );

        when(pokemonSetCatalogRepository
                .saveAll(anyList()))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        List<PokemonSetCatalogEntity> result =
                pokemonSetCatalogService.findAll();

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Base",
                result.get(0).getName()
        );

        assertEquals(
                "Jungle",
                result.get(1).getName()
        );

        PokemonSetCatalogEntity baseResult =
                result.get(0);

        assertEquals(
                "base1",
                baseResult.getCollectionId()
        );

        assertEquals(
                "Base",
                baseResult.getSeries()
        );

        assertEquals(
                102,
                baseResult.getPrintedTotal()
        );

        assertEquals(
                102,
                baseResult.getTotal()
        );

        assertNotNull(
                baseResult.getLastSyncedAt()
        );

        verify(pokemonTcgClient)
                .findSets();

        verify(pokemonSetCatalogRepository)
                .saveAll(anyList());
    }

    @Test
    void shouldRefreshExpiredCache() {
        PokemonSetCatalogEntity cachedSet =
                createCachedSet(
                        "base1",
                        "Old Base Name",
                        "Base",
                        102,
                        102,
                        LocalDateTime.now()
                                .minusHours(25)
                );

        PokemonSetApiData apiSet =
                new PokemonSetApiData(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102
                );

        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of(cachedSet)
                );

        when(pokemonTcgClient.findSets())
                .thenReturn(
                        new PokemonSetApiResponse(
                                List.of(apiSet)
                        )
                );

        when(pokemonSetCatalogRepository
                .saveAll(anyList()))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        List<PokemonSetCatalogEntity> result =
                pokemonSetCatalogService.findAll();

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Base",
                result.get(0).getName()
        );

        verify(pokemonTcgClient)
                .findSets();

        verify(pokemonSetCatalogRepository)
                .saveAll(anyList());
    }

    @Test
    void shouldReturnStaleCacheWhenPokemonApiIsUnavailable() {
        PokemonSetCatalogEntity cachedSet =
                createCachedSet(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102,
                        LocalDateTime.now()
                                .minusHours(25)
                );

        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of(cachedSet)
                );

        when(pokemonTcgClient.findSets())
                .thenThrow(
                        new PokemonTcgApiUnavailableException()
                );

        List<PokemonSetCatalogEntity> result =
                pokemonSetCatalogService.findAll();

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "base1",
                result.get(0).getCollectionId()
        );

        verify(
                pokemonSetCatalogRepository,
                never()
        ).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenCacheIsEmptyAndPokemonApiIsUnavailable() {
        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of()
                );

        when(pokemonTcgClient.findSets())
                .thenThrow(
                        new PokemonTcgApiUnavailableException()
                );

        assertThrows(
                PokemonTcgApiUnavailableException.class,
                () -> pokemonSetCatalogService
                        .findAll()
        );

        verify(
                pokemonSetCatalogRepository,
                never()
        ).saveAll(anyList());
    }

    @Test
    void shouldReturnStaleCacheWhenPokemonApiReturnsEmptyResponse() {
        PokemonSetCatalogEntity cachedSet =
                createCachedSet(
                        "base1",
                        "Base",
                        "Base",
                        102,
                        102,
                        LocalDateTime.now()
                                .minusHours(25)
                );

        when(pokemonSetCatalogRepository
                .findAllByOrderByNameAsc())
                .thenReturn(
                        List.of(cachedSet)
                );

        when(pokemonTcgClient.findSets())
                .thenReturn(
                        new PokemonSetApiResponse(
                                List.of()
                        )
                );

        List<PokemonSetCatalogEntity> result =
                pokemonSetCatalogService.findAll();

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "base1",
                result.get(0).getCollectionId()
        );

        verify(
                pokemonSetCatalogRepository,
                never()
        ).saveAll(anyList());
    }

    private PokemonSetCatalogEntity createCachedSet(
            String collectionId,
            String name,
            String series,
            Integer printedTotal,
            Integer total,
            LocalDateTime lastSyncedAt
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
                lastSyncedAt
        );

        return entity;
    }
}