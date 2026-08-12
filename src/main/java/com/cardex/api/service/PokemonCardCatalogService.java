package com.cardex.api.service;

import com.cardex.api.entity.PokemonCardCatalogEntity;

import java.util.List;

public interface PokemonCardCatalogService {

    List<PokemonCardCatalogEntity> findByCollectionId(
            String collectionId
    );
}