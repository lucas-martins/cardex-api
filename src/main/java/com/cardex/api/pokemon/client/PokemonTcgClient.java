package com.cardex.api.pokemon.client;

import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PokemonTcgClient {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RestClient pokemonTcgRestClient;

    public PokemonCardApiResponse searchByName(String name) {
        return pokemonTcgRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards")
                        .queryParam("q", "name:" + name)
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .queryParam(
                                "select",
                                "id,name,number,rarity,set,images"
                        )
                        .build())
                .retrieve()
                .body(PokemonCardApiResponse.class);
    }
}