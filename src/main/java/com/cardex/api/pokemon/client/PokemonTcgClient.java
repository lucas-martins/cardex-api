package com.cardex.api.pokemon.client;

import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;
import com.cardex.api.pokemon.dto.PokemonSetApiResponse;

@Component
@RequiredArgsConstructor
public class PokemonTcgClient {

    private static final String SELECTED_FIELDS =
            "id,name,number,rarity,set,images,tcgplayer,cardmarket";

    private static final int MAX_ATTEMPTS = 3;

    private static final long RETRY_DELAY_MILLISECONDS = 500L;

    private final RestClient pokemonTcgRestClient;

    public PokemonCardApiResponse searchCards(
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int pageSize
    ) {
        String query = buildSearchQuery(name, setId, number, rarity);

        return executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder
                                    .path("/cards")
                                    .queryParam("page", page)
                                    .queryParam("pageSize", pageSize)
                                    .queryParam("select", SELECTED_FIELDS);

                            if (query != null && !query.isBlank()) {
                                builder = builder.queryParam("q", query);
                            }

                            return builder.build();
                        })
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(
                                PokemonCardApiResponse.class
                        )
        );
    }

    public PokemonCardApiResponse searchByName(
            String name,
            int page,
            int pageSize
    ) {
        return searchCards(name, null, null, null, page, pageSize);
    }

    private String buildSearchQuery(
            String name,
            String setId,
            String number,
            String rarity
    ) {
        java.util.List<String> clauses = new java.util.ArrayList<>();

        if (name != null && !name.isBlank()) {
            clauses.add("name:*" + name.trim() + "*");
        }

        if (setId != null && !setId.isBlank()) {
            clauses.add("set.id:" + setId.trim());
        }

        if (number != null && !number.isBlank()) {
            clauses.add("number:" + number.trim());
        }

        if (rarity != null && !rarity.isBlank()) {
            String normalizedRarity = rarity.trim();

            if (normalizedRarity.contains(" ")) {
                clauses.add("rarity:\"" + normalizedRarity + "\"");
            } else {
                clauses.add("rarity:" + normalizedRarity);
            }
        }

        if (clauses.isEmpty()) {
            return null;
        }

        return String.join(" ", clauses);
    }

    public PokemonCardApiSingleResponse findById(
            String externalId
    ) {
        return executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/{id}")
                                .queryParam(
                                        "select",
                                        SELECTED_FIELDS
                                )
                                .build(externalId)
                        )
                        .retrieve()
                        .onStatus(
                                status ->
                                        status.value() == 404,
                                (request, response) -> {
                                    throw new PokemonCardNotFoundException(
                                            externalId
                                    );
                                }
                        )
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(
                                PokemonCardApiSingleResponse.class
                        )
        );
    }

    public PokemonCardApiResponse searchBySetId(
            String collectionId,
            int page,
            int pageSize
    ) {
        return executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards")
                                .queryParam(
                                        "q",
                                        "set.id:"
                                                + collectionId
                                )
                                .queryParam(
                                        "page",
                                        page
                                )
                                .queryParam(
                                        "pageSize",
                                        pageSize
                                )
                                .queryParam(
                                        "select",
                                        SELECTED_FIELDS
                                )
                                .build()
                        )
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(
                                PokemonCardApiResponse.class
                        )
        );
    }

    public PokemonSetApiResponse findSets() {
        return executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/sets")
                                .queryParam(
                                        "orderBy",
                                        "name"
                                )
                                .build()
                        )
                        .retrieve()
                        .onStatus(
                                status ->
                                        status.value() == 429
                                                || status
                                                .is5xxServerError(),
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(
                                PokemonSetApiResponse.class
                        )
        );
    }

    private <T> T executeWithRetry(
            Supplier<T> request
    ) {
        for (
                int attempt = 1;
                attempt <= MAX_ATTEMPTS;
                attempt++
        ) {
            try {
                return request.get();
            } catch (
                    PokemonTcgApiUnavailableException
                    | ResourceAccessException exception
            ) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new PokemonTcgApiUnavailableException();
                }

                waitBeforeRetry(attempt);
            }
        }

        throw new PokemonTcgApiUnavailableException();
    }

    private void waitBeforeRetry(
            int attempt
    ) {
        try {
            Thread.sleep(
                    RETRY_DELAY_MILLISECONDS
                            * attempt
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new PokemonTcgApiUnavailableException();
        }
    }
}