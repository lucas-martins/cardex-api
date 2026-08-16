package com.cardex.api.service.impl;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.repository.PokemonCardCatalogRepository;
import com.cardex.api.service.PokemonCardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PokemonCardCatalogServiceImpl
        implements PokemonCardCatalogService {

    private static final int PAGE_SIZE = 250;

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

        if (!cachedCards.isEmpty()) {
            return cachedCards;
        }

        List<PokemonCardApiData> pokemonCards =
                findAllByCollectionId(collectionId);

        List<PokemonCardCatalogEntity> catalogCards =
                pokemonCards.stream()
                        .map(this::toEntity)
                        .toList();

        return pokemonCardCatalogRepository
                .saveAll(catalogCards);
    }

    @Override
    @Transactional
    public PokemonCardCatalogEntity findByExternalId(
            String externalId
    ) {
        return pokemonCardCatalogRepository
                .findByExternalId(externalId)
                .orElseGet(() -> loadAndSaveByExternalId(
                        externalId
                ));
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

        return entity;
    }
}