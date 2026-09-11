package com.cardex.api.pokemon.service;

import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import com.cardex.api.pokemon.response.PokemonCollectionResponse;

import java.util.List;

public interface PokemonCardService {

    PokemonCardSearchPageResponse search(
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int size
    );

    List<PokemonCollectionResponse> findCollections();
}