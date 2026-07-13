package com.cardex.api.pokemon.service.impl;

import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.mapper.PokemonCardMapper;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import com.cardex.api.pokemon.service.PokemonCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PokemonCardServiceImpl implements PokemonCardService {

    private final PokemonTcgClient pokemonTcgClient;
    private final PokemonCardMapper pokemonCardMapper;

    @Override
    public List<PokemonCardSearchResponse> searchByName(String name) {
        PokemonCardApiResponse apiResponse = pokemonTcgClient.searchByName(name);

        if (apiResponse == null || apiResponse.data() == null) {
            return Collections.emptyList();
        }

        return apiResponse.data()
                .stream()
                .map(pokemonCardMapper::toSearchResponse)
                .toList();
    }
}