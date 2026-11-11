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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
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

        if (isCacheFresh(cachedSets)
                && !hasDuplicateSetNames(cachedSets)
                && !hasMissingAbbreviations(cachedSets)) {
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

    @Override
    @Transactional
    public Optional<PokemonSetCatalogEntity> findByCollectionId(
            String collectionId
    ) {
        return findAll()
                .stream()
                .filter(set ->
                        set.getCollectionId()
                                .equals(collectionId)
                )
                .findFirst();
    }

    @Override
    @Transactional
    public void enrichMissingAbbreviations() {
        List<PokemonSetCatalogEntity> missingSets =
                pokemonSetCatalogRepository
                        .findAllByOrderByNameAsc()
                        .stream()
                        .filter(set ->
                                set.getAbbreviation() == null
                        )
                        .toList();

        if (missingSets.isEmpty()) {
            return;
        }

        Map<String, String> abbreviationsBySetId =
                new ConcurrentHashMap<>();

        try (var executor = Executors.newFixedThreadPool(
                Math.min(8, missingSets.size())
        )) {
            List<CompletableFuture<Void>> futures =
                    missingSets.stream()
                            .map(set ->
                                    CompletableFuture.runAsync(
                                            () -> {
                                                String abbreviation =
                                                        pokemonTcgClient
                                                                .findSetAbbreviation(
                                                                        set.getCollectionId()
                                                                );

                                                abbreviationsBySetId.put(
                                                        set.getCollectionId(),
                                                        abbreviation == null
                                                                ? ""
                                                                : abbreviation
                                                );
                                            },
                                            executor
                                    )
                            )
                            .toList();

            futures.forEach(CompletableFuture::join);
        }

        for (PokemonSetCatalogEntity set : missingSets) {
            set.setAbbreviation(
                    abbreviationsBySetId.getOrDefault(
                            set.getCollectionId(),
                            ""
                    )
            );
        }

        pokemonSetCatalogRepository.saveAll(missingSets);
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
                .map(PokemonSetCatalogEntity::getLastSyncedAt)
                .filter(lastSyncedAt -> lastSyncedAt != null)
                .max(LocalDateTime::compareTo)
                .map(lastSyncedAt ->
                        lastSyncedAt.isAfter(minimumValidDate)
                )
                .orElse(false);
    }

    private boolean hasMissingAbbreviations(
            List<PokemonSetCatalogEntity> cachedSets
    ) {
        if (cachedSets.isEmpty()) {
            return false;
        }

        long missingAbbreviation = cachedSets.stream()
                .filter(set -> set.getAbbreviation() == null)
                .count();

        return missingAbbreviation > 0;
    }

    private boolean hasDuplicateSetNames(
            List<PokemonSetCatalogEntity> cachedSets
    ) {
        return cachedSets
                .stream()
                .collect(
                        Collectors.groupingBy(
                                this::setIdentityKey,
                                Collectors.counting()
                        )
                )
                .values()
                .stream()
                .anyMatch(count -> count > 1);
    }

    private String setIdentityKey(
            PokemonSetCatalogEntity set
    ) {
        String name = set.getName() == null
                ? ""
                : set.getName().trim().toLowerCase();

        String series = set.getSeries() == null
                ? ""
                : set.getSeries().trim().toLowerCase();

        return name + "|" + series;
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

        List<PokemonSetApiData> apiSets =
                apiResponse.data();

        List<PokemonSetCatalogEntity> sets =
                apiSets
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

        Set<String> apiCollectionIds =
                apiSets
                        .stream()
                        .map(PokemonSetApiData::id)
                        .filter(id -> id != null && !id.isBlank())
                        .collect(Collectors.toSet());

        List<PokemonSetCatalogEntity> orphanSets =
                cachedSets
                        .stream()
                        .filter(cachedSet ->
                                cachedSet.getCollectionId() != null
                                        && !apiCollectionIds.contains(
                                        cachedSet.getCollectionId()
                                )
                        )
                        .toList();

        if (!orphanSets.isEmpty()) {
            pokemonSetCatalogRepository.deleteAll(
                    orphanSets
            );
        }

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

        if (set.abbreviation() != null
                && !set.abbreviation().isBlank()) {
            entity.setAbbreviation(set.abbreviation());
        } else if (entity.getAbbreviation() == null) {
            entity.setAbbreviation("");
        }

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