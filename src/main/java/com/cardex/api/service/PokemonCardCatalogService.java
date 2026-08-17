package com.cardex.api.service;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PokemonCardCatalogService {

    List<PokemonCardCatalogEntity> findByCollectionId(
            String collectionId
    );

    PokemonCardCatalogEntity findByExternalId(
            String externalId
    );

    void cacheCards(
            List<PokemonCardApiData> cards
    );

    Page<PokemonCardCatalogEntity> searchByName(
            String name,
            int page,
            int size
    );
}