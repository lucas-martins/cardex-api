package com.cardex.api.service.impl;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.PokemonCardPriceExtractor;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.repository.PokemonCardCatalogRepository;
import com.cardex.api.service.PokemonCardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokemonCardCatalogServiceImpl
        implements PokemonCardCatalogService {

    private static final int PAGE_SIZE = 250;

    private static final int PRICE_STALE_DAYS = 7;

    private final PokemonCardCatalogRepository
            pokemonCardCatalogRepository;

    private final PokemonTcgClient pokemonTcgClient;

    @Override
    @Transactional
    public List<PokemonCardCatalogEntity> findByCollectionId(
            String collectionId
    ) {
        List<PokemonCardCatalogEntity> cachedCards =
                pokemonCardCatalogRepository
                        .findByCollectionId(collectionId);

        if (isCollectionCacheComplete(cachedCards)) {
            if (needsCollectionPriceRefresh(cachedCards)) {
                refreshCollectionPrices(collectionId, cachedCards);

                return pokemonCardCatalogRepository
                        .findByCollectionId(collectionId);
            }

            return cachedCards;
        }

        List<PokemonCardApiData> pokemonCards =
                findAllByCollectionId(collectionId);

        Set<String> cachedExternalIds =
                cachedCards
                        .stream()
                        .map(
                                PokemonCardCatalogEntity::getExternalId
                        )
                        .collect(Collectors.toSet());

        List<PokemonCardCatalogEntity> newCards =
                pokemonCards
                        .stream()
                        .filter(card ->
                                !cachedExternalIds.contains(
                                        card.id()
                                )
                        )
                        .map(this::toEntity)
                        .toList();

        if (!newCards.isEmpty()) {
            pokemonCardCatalogRepository
                    .saveAll(newCards);
        }

        return pokemonCardCatalogRepository
                .findByCollectionId(collectionId);
    }

    private boolean isCollectionCacheComplete(
            List<PokemonCardCatalogEntity> cachedCards
    ) {
        if (cachedCards.isEmpty()) {
            return false;
        }

        Integer expectedTotal =
                cachedCards
                        .stream()
                        .map(
                                PokemonCardCatalogEntity::getCollectionTotal
                        )
                        .filter(total -> total != null)
                        .findFirst()
                        .orElse(null);

        return expectedTotal != null
                && cachedCards.size() >= expectedTotal;
    }

    private boolean needsCollectionPriceRefresh(
            List<PokemonCardCatalogEntity> cachedCards
    ) {
        return cachedCards
                .stream()
                .anyMatch(card ->
                        card.getPriceUpdatedAt() == null
                );
    }

    private void refreshCollectionPrices(
            String collectionId,
            List<PokemonCardCatalogEntity> cachedCards
    ) {
        List<PokemonCardApiData> pokemonCards;

        try {
            pokemonCards =
                    findAllByCollectionId(collectionId);
        } catch (PokemonTcgApiUnavailableException exception) {
            markPricesChecked(cachedCards);
            return;
        }

        Map<String, PokemonCardCatalogEntity> cachedByExternalId =
                cachedCards
                        .stream()
                        .collect(Collectors.toMap(
                                PokemonCardCatalogEntity::getExternalId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        List<PokemonCardCatalogEntity> updatedCards =
                new ArrayList<>();

        for (PokemonCardApiData card : pokemonCards) {
            PokemonCardCatalogEntity cachedCard =
                    cachedByExternalId.get(card.id());

            if (cachedCard == null) {
                continue;
            }

            PokemonCardPriceExtractor.applyToCatalog(
                    cachedCard,
                    card
            );

            updatedCards.add(cachedCard);
        }

        Set<String> refreshedExternalIds =
                pokemonCards
                        .stream()
                        .map(PokemonCardApiData::id)
                        .collect(Collectors.toSet());

        List<PokemonCardCatalogEntity> uncheckedCards =
                cachedCards
                        .stream()
                        .filter(card ->
                                !refreshedExternalIds.contains(
                                        card.getExternalId()
                                )
                        )
                        .toList();

        markPricesChecked(uncheckedCards);
        updatedCards.addAll(uncheckedCards);

        if (!updatedCards.isEmpty()) {
            pokemonCardCatalogRepository.saveAll(
                    updatedCards
            );
        }
    }

    private void markPricesChecked(
            List<PokemonCardCatalogEntity> cards
    ) {
        LocalDateTime now = LocalDateTime.now();

        for (PokemonCardCatalogEntity card : cards) {
            if (card.getPriceUpdatedAt() == null) {
                card.setPriceUpdatedAt(now);
            }
        }
    }

    @Override
    @Transactional
    public PokemonCardCatalogEntity findByExternalId(
            String externalId
    ) {
        return pokemonCardCatalogRepository
                .findByExternalId(externalId)
                .map(this::refreshPricesIfNeeded)
                .orElseGet(() -> loadAndSaveByExternalId(
                        externalId
                ));
    }

    private PokemonCardCatalogEntity refreshPricesIfNeeded(
            PokemonCardCatalogEntity cachedCard
    ) {
        if (!isPriceStale(cachedCard)) {
            return cachedCard;
        }

        try {
            PokemonCardApiSingleResponse response =
                    pokemonTcgClient.findById(
                            cachedCard.getExternalId()
                    );

            if (response != null
                    && response.data() != null) {
                PokemonCardPriceExtractor.applyToCatalog(
                        cachedCard,
                        response.data()
                );

                return pokemonCardCatalogRepository.save(
                        cachedCard
                );
            }
        } catch (PokemonTcgApiUnavailableException
                 | PokemonCardNotFoundException exception) {
            cachedCard.setPriceUpdatedAt(
                    LocalDateTime.now()
            );

            return pokemonCardCatalogRepository.save(
                    cachedCard
            );
        }

        cachedCard.setPriceUpdatedAt(
                LocalDateTime.now()
        );

        return pokemonCardCatalogRepository.save(
                cachedCard
        );
    }

    private boolean isPriceStale(
            PokemonCardCatalogEntity cachedCard
    ) {
        if (cachedCard.getPriceUpdatedAt() == null) {
            return true;
        }

        return cachedCard
                .getPriceUpdatedAt()
                .isBefore(
                        LocalDateTime.now().minusDays(
                                PRICE_STALE_DAYS
                        )
                );
    }

    private PokemonCardCatalogEntity loadAndSaveByExternalId(
            String externalId
    ) {
        PokemonCardApiSingleResponse response =
                pokemonTcgClient.findById(
                        externalId
                );

        if (response == null
                || response.data() == null) {
            throw new PokemonCardNotFoundException(
                    externalId
            );
        }

        PokemonCardCatalogEntity catalogCard =
                toEntity(
                        response.data()
                );

        return pokemonCardCatalogRepository.save(
                catalogCard
        );
    }

    private List<PokemonCardApiData> findAllByCollectionId(
            String collectionId
    ) {
        List<PokemonCardApiData> cards =
                new ArrayList<>();

        int page = 1;

        while (true) {
            PokemonCardApiResponse response =
                    pokemonTcgClient.searchBySetId(
                            collectionId,
                            page,
                            PAGE_SIZE
                    );

            if (response == null
                    || response.data() == null
                    || response.data().isEmpty()) {
                break;
            }

            cards.addAll(response.data());

            if (response.totalCount() == null
                    || cards.size() >= response.totalCount()) {
                break;
            }
            page++;
        }

        return cards;
    }

    private PokemonCardCatalogEntity toEntity(
            PokemonCardApiData card
    ) {
        PokemonCardCatalogEntity entity =
                new PokemonCardCatalogEntity();

        entity.setExternalId(card.id());
        entity.setName(card.name());
        entity.setCardNumber(card.number());
        entity.setRarity(card.rarity());

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

        PokemonCardPriceExtractor.applyToCatalog(
                entity,
                card
        );

        return entity;
    }

    @Override
    @Transactional
    public void cacheCards(
            List<PokemonCardApiData> cards
    ) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        List<String> externalIds =
                cards.stream()
                        .map(PokemonCardApiData::id)
                        .distinct()
                        .toList();

        List<PokemonCardCatalogEntity> existingCards =
                pokemonCardCatalogRepository
                        .findAllByExternalIdIn(
                                externalIds
                        );

        Map<String, PokemonCardCatalogEntity> existingByExternalId =
                existingCards
                        .stream()
                        .collect(Collectors.toMap(
                                PokemonCardCatalogEntity::getExternalId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        List<PokemonCardCatalogEntity> cardsToSave =
                new ArrayList<>();

        for (PokemonCardApiData card : cards) {
            PokemonCardCatalogEntity existingCard =
                    existingByExternalId.get(card.id());

            if (existingCard == null) {
                cardsToSave.add(toEntity(card));
                continue;
            }

            PokemonCardPriceExtractor.applyToCatalog(
                    existingCard,
                    card
            );

            cardsToSave.add(existingCard);
        }

        if (!cardsToSave.isEmpty()) {
            pokemonCardCatalogRepository.saveAll(
                    cardsToSave
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PokemonCardCatalogEntity> searchByName(
            String name,
            int page,
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        Math.max(page - 1, 0),
                        size,
                        Sort.by(
                                Sort.Order.asc("name")
                                        .ignoreCase()
                        )
                );

        return pokemonCardCatalogRepository
                .findByNameContainingIgnoreCase(
                        name.trim(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PokemonCardCatalogEntity> findAllByExternalIdIn(
            List<String> externalIds
    ) {
        if (externalIds == null || externalIds.isEmpty()) {
            return List.of();
        }

        return pokemonCardCatalogRepository
                .findAllByExternalIdIn(
                        externalIds
                );
    }
}