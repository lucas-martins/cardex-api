package com.cardex.api.service.impl;

import com.cardex.api.entity.PokemonSetCatalogEntity;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiResponse;
import com.cardex.api.repository.PokemonSetCatalogRepository;
import com.cardex.api.service.PokemonSetCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokemonSetCatalogServiceImpl
        implements PokemonSetCatalogService {

    private static final Duration CACHE_TTL =
            Duration.ofHours(24);

    private final PokemonSetCatalogRepository
            pokemonSetCatalogRepository;

    private final PokemonTcgClient pokemonTcgClient;

    @Override
    @Transactional
    public List<PokemonSetCatalogEntity> findAll() {
        List<PokemonSetCatalogEntity> cachedSets =
                pokemonSetCatalogRepository
                        .findAllByOrderByNameAsc();

        if (isCacheFresh(cachedSets)) {
            return cachedSets;
        }

        try {
            return refreshCache(cachedSets);
        } catch (
                PokemonTcgApiUnavailableException exception
        ) {
            if (!cachedSets.isEmpty()) {
                return cachedSets;
            }

            throw exception;
        }
    }

    private boolean isCacheFresh(
            List<PokemonSetCatalogEntity> cachedSets
    ) {
        if (cachedSets.isEmpty()) {
            return false;
        }

        LocalDateTime minimumValidDate =
                LocalDateTime.now()
                        .minus(CACHE_TTL);

        return cachedSets
                .stream()
                .allMatch(set ->
                        set.getLastSyncedAt() != null
                                && set.getLastSyncedAt()
                                .isAfter(
                                        minimumValidDate
                                )
                );
    }

    private List<PokemonSetCatalogEntity> refreshCache(
            List<PokemonSetCatalogEntity> cachedSets
    ) {
        PokemonSetApiResponse apiResponse =
                pokemonTcgClient.findSets();

        if (apiResponse == null
                || apiResponse.data() == null
                || apiResponse.data().isEmpty()) {
            if (!cachedSets.isEmpty()) {
                return cachedSets;
            }

            return List.of();
        }

        Map<String, PokemonSetCatalogEntity>
                cachedSetsByCollectionId =
                cachedSets
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PokemonSetCatalogEntity
                                                ::getCollectionId,
                                        Function.identity()
                                )
                        );

        LocalDateTime syncedAt =
                LocalDateTime.now();

        List<PokemonSetCatalogEntity> sets =
                apiResponse
                        .data()
                        .stream()
                        .map(set ->
                                toEntity(
                                        set,
                                        cachedSetsByCollectionId,
                                        syncedAt
                                )
                        )
                        .toList();

        pokemonSetCatalogRepository
                .saveAll(sets);

        return sets
                .stream()
                .sorted(
                        (first, second) ->
                                first.getName()
                                        .compareToIgnoreCase(
                                                second.getName()
                                        )
                )
                .toList();
    }

    private PokemonSetCatalogEntity toEntity(
            PokemonSetApiData set,
            Map<String, PokemonSetCatalogEntity>
                    cachedSetsByCollectionId,
            LocalDateTime syncedAt
    ) {
        PokemonSetCatalogEntity entity =
                cachedSetsByCollectionId
                        .getOrDefault(
                                set.id(),
                                new PokemonSetCatalogEntity()
                        );

        entity.setCollectionId(
                set.id()
        );

        entity.setName(
                set.name()
        );

        entity.setSeries(
                set.series()
        );

        entity.setPrintedTotal(
                set.printedTotal()
        );

        entity.setTotal(
                set.total()
        );

        entity.setLastSyncedAt(
                syncedAt
        );

        return entity;
    }
}