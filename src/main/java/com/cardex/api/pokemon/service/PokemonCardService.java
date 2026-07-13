package com.cardex.api.pokemon.service;

import com.cardex.api.pokemon.response.PokemonCardSearchResponse;

import java.util.List;

public interface PokemonCardService {

    List<PokemonCardSearchResponse> searchByName(String name);
}