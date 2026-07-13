package com.cardex.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class PokemonTcgClientConfig {

    private static final String API_KEY_HEADER = "X-Api-Key";

    @Bean
    public RestClient pokemonTcgRestClient(
            @Value("${pokemon-tcg.base-url}") String baseUrl,
            @Value("${pokemon-tcg.api-key}") String apiKey
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(API_KEY_HEADER, apiKey)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}