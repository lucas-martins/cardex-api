package com.cardex.api.service;

import com.cardex.api.entity.PokemonSetCatalogEntity;

import java.util.List;

public interface PokemonSetCatalogService {

    List<PokemonSetCatalogEntity> findAll();
}