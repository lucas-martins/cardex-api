package com.cardex.api.pokemon.service.impl;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.mapper.PokemonCardMapper;
import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import com.cardex.api.pokemon.service.PokemonCardService;
import com.cardex.api.service.PokemonCardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PokemonCardServiceImpl
        implements PokemonCardService {

    private final PokemonTcgClient pokemonTcgClient;
    private final PokemonCardMapper pokemonCardMapper;
    private final PokemonCardCatalogService
            pokemonCardCatalogService;

    @Override
    public PokemonCardSearchPageResponse searchByName(
            String name,
            int page,
            int size
    ) {
        try {
            PokemonCardApiResponse apiResponse =
                    pokemonTcgClient.searchByName(
                            name,
                            page,
                            size
                    );

            if (apiResponse == null
                    || apiResponse.data() == null) {
                return emptyResponse(
                        page,
                        size
                );
            }

            pokemonCardCatalogService.cacheCards(
                    apiResponse.data()
            );

            List<PokemonCardSearchResponse> cards =
                    apiResponse.data()
                            .stream()
                            .map(
                                    pokemonCardMapper::toSearchResponse
                            )
                            .toList();

            int totalElements =
                    apiResponse.totalCount() != null
                            ? apiResponse.totalCount()
                            : 0;

            int totalPages =
                    size > 0
                            ? (int) Math.ceil(
                            (double) totalElements
                            / size
                    )
                            : 0;

            return new PokemonCardSearchPageResponse(
                    cards,
                    apiResponse.page(),
                    apiResponse.pageSize(),
                    apiResponse.count(),
                    totalElements,
                    totalPages,
                    apiResponse.page() == 1,
                    totalPages == 0
                            || apiResponse.page()
                            >= totalPages
            );
        } catch (
                PokemonTcgApiUnavailableException exception
        ) {
            return searchFromLocalCatalog(
                    name,
                    page,
                    size,
                    exception
            );
        }
    }

    private PokemonCardSearchPageResponse searchFromLocalCatalog(
            String name,
            int page,
            int size,
            PokemonTcgApiUnavailableException exception
    ) {
        Page<PokemonCardCatalogEntity> localPage =
                pokemonCardCatalogService.searchByName(
                        name,
                        page,
                        size
                );

        if (localPage.isEmpty()) {
            throw exception;
        }

        List<PokemonCardSearchResponse> cards =
                localPage
                        .getContent()
                        .stream()
                        .map(
                                pokemonCardMapper::toSearchResponse
                        )
                        .toList();

        return new PokemonCardSearchPageResponse(
                cards,
                page,
                size,
                cards.size(),
                (int) localPage.getTotalElements(),
                localPage.getTotalPages(),
                localPage.isFirst(),
                localPage.isLast()
        );
    }

    private PokemonCardSearchPageResponse emptyResponse(
            int page,
            int size
    ) {
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
}