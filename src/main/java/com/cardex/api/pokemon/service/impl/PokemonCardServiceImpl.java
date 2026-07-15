package com.cardex.api.pokemon.service.impl;

import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.mapper.PokemonCardMapper;
import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
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
    public PokemonCardSearchPageResponse searchByName(
            String name,
            int page,
            int size
    ) {
        PokemonCardApiResponse apiResponse =
                pokemonTcgClient.searchByName(name, page, size);

        if (apiResponse == null || apiResponse.data() == null) {
            return new PokemonCardSearchPageResponse(
                    List.of(),
                    page,
                    size,
                    0,
                    0,
                    0,
                    page == 1,
                    true
            );
        }

        List<PokemonCardSearchResponse> cards = apiResponse.data()
                .stream()
                .map(pokemonCardMapper::toSearchResponse)
                .toList();

        int totalElements = apiResponse.totalCount() != null
                ? apiResponse.totalCount()
                : 0;

        int totalPages = size > 0
                ? (int) Math.ceil((double) totalElements / size)
                : 0;

        return new PokemonCardSearchPageResponse(
                cards,
                apiResponse.page(),
                apiResponse.pageSize(),
                apiResponse.count(),
                totalElements,
                totalPages,
                apiResponse.page() == 1,
                totalPages == 0 || apiResponse.page() >= totalPages
        );
    }
}