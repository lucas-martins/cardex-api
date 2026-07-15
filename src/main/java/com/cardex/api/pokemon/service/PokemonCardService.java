package com.cardex.api.pokemon.service;

import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;

import java.util.List;

public interface PokemonCardService {

    PokemonCardSearchPageResponse searchByName(
            String name,
            int page,
            int size
    );
}