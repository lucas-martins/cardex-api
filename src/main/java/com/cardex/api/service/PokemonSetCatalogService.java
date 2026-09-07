package com.cardex.api.service;

import com.cardex.api.entity.PokemonSetCatalogEntity;

import java.util.List;
import java.util.Optional;

public interface PokemonSetCatalogService {

    List<PokemonSetCatalogEntity> findAll();

    Optional<PokemonSetCatalogEntity> findByCollectionId(
            String collectionId
    );
}