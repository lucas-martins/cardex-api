package com.cardex.api.pokemon.client;

import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PokemonTcgClient {

    private static final String SELECTED_FIELDS =
            "id,name,number,rarity,set,images";

    private final RestClient pokemonTcgRestClient;

    public PokemonCardApiResponse searchByName(
            String name,
            int page,
            int pageSize
    ) {
        String normalizedName = name.trim();

        return pokemonTcgRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards")
                        .queryParam("q", "name:*" + normalizedName + "*")
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("select", SELECTED_FIELDS)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.is5xxServerError(),
                        (request, response) -> {
                            throw new PokemonTcgApiUnavailableException();
                        }
                )
                .body(PokemonCardApiResponse.class);
    }

    public PokemonCardApiSingleResponse findById(String externalId) {
        return pokemonTcgRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards/{id}")
                        .queryParam("select", SELECTED_FIELDS)
                        .build(externalId))
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new PokemonCardNotFoundException(externalId);
                        }
                )
                .body(PokemonCardApiSingleResponse.class);
    }
}