package com.cardex.api.pokemon.client;

import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiResponse;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiResponse;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexCardDetail;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSerieBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSerieDetail;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSetBrief;
import com.cardex.api.pokemon.dto.tcgdex.TcgdexSetDetail;
import com.cardex.api.pokemon.mapper.TcgdexCardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class PokemonTcgClient {

    private static final int MAX_ATTEMPTS = 3;

    private static final long RETRY_DELAY_MILLISECONDS = 500L;

    private static final int HYDRATION_PARALLELISM = 8;

    private static final ParameterizedTypeReference<List<TcgdexCardBrief>>
            CARD_BRIEF_LIST_TYPE = new ParameterizedTypeReference<>() {
    };

    private static final ParameterizedTypeReference<List<TcgdexSerieBrief>>
            SERIE_BRIEF_LIST_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RestClient pokemonTcgRestClient;

    private final TcgdexCardMapper tcgdexCardMapper;

    public PokemonCardApiResponse searchCards(
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int pageSize
    ) {
        List<TcgdexCardBrief> briefs = executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> buildCardSearchUri(
                                uriBuilder,
                                name,
                                setId,
                                number,
                                rarity,
                                page,
                                pageSize
                        ))
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(CARD_BRIEF_LIST_TYPE)
        );

        if (briefs == null) {
            briefs = List.of();
        }

        List<PokemonCardApiData> cards = hydrateCards(briefs);

        return toPagedResponse(cards, page, pageSize, briefs.size());
    }

    public PokemonCardApiResponse searchByName(
            String name,
            int page,
            int pageSize
    ) {
        return searchCards(name, null, null, null, page, pageSize);
    }

    public PokemonCardApiSingleResponse findById(String externalId) {
        TcgdexCardDetail detail = executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/{id}")
                                .build(externalId)
                        )
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
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
                        .body(TcgdexCardDetail.class)
        );

        if (detail == null) {
            throw new PokemonCardNotFoundException(externalId);
        }

        return new PokemonCardApiSingleResponse(
                tcgdexCardMapper.toCardApiData(detail)
        );
    }

    public PokemonCardApiResponse searchBySetId(
            String collectionId,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        TcgdexSetDetail setDetail;

        try {
            setDetail = executeWithRetry(
                    () -> pokemonTcgRestClient
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/sets/{id}")
                                    .build(collectionId)
                            )
                            .retrieve()
                            .onStatus(
                                    status -> status.value() == 404,
                                    (request, response) -> {
                                        throw new PokemonCardNotFoundException(
                                                collectionId
                                        );
                                    }
                            )
                            .onStatus(
                                    HttpStatusCode::is5xxServerError,
                                    (request, response) -> {
                                        throw new PokemonTcgApiUnavailableException();
                                    }
                            )
                            .body(TcgdexSetDetail.class)
            );
        } catch (PokemonCardNotFoundException exception) {
            return new PokemonCardApiResponse(
                    List.of(),
                    safePage,
                    safePageSize,
                    0,
                    0
            );
        }

        List<TcgdexCardBrief> allCards = setDetail == null
                || setDetail.cards() == null
                ? List.of()
                : setDetail.cards();

        int fromIndex = Math.min((safePage - 1) * safePageSize, allCards.size());
        int toIndex = Math.min(fromIndex + safePageSize, allCards.size());

        List<TcgdexCardBrief> pageBriefs =
                allCards.subList(fromIndex, toIndex);

        String seriesName = setDetail != null
                && setDetail.serie() != null
                ? setDetail.serie().name()
                : null;

        TcgdexCardMapper.TcgdexSetDetailContext setContext =
                TcgdexCardMapper.TcgdexSetDetailContext.from(
                        setDetail == null
                                ? null
                                : new TcgdexSetBrief(
                                        setDetail.id(),
                                        setDetail.name(),
                                        setDetail.cardCount(),
                                        setDetail.serie()
                                ),
                        seriesName
                );

        List<PokemonCardApiData> cards = hydrateCards(pageBriefs, setContext);

        return new PokemonCardApiResponse(
                cards,
                safePage,
                safePageSize,
                cards.size(),
                allCards.size()
        );
    }

    public PokemonSetApiResponse findSets() {
        List<TcgdexSerieBrief> series = executeWithRetry(
                () -> pokemonTcgRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/series")
                                .build()
                        )
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 429
                                        || status.is5xxServerError(),
                                (request, response) -> {
                                    throw new PokemonTcgApiUnavailableException();
                                }
                        )
                        .body(SERIE_BRIEF_LIST_TYPE)
        );

        if (series == null || series.isEmpty()) {
            return new PokemonSetApiResponse(List.of());
        }

        List<PokemonSetApiData> sets = new ArrayList<>();

        for (TcgdexSerieBrief serie : series) {
            if (serie == null || serie.id() == null || serie.id().isBlank()) {
                continue;
            }

            TcgdexSerieDetail detail;

            try {
                detail = executeWithRetry(
                        () -> pokemonTcgRestClient
                                .get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/series/{id}")
                                        .build(serie.id())
                                )
                                .retrieve()
                                .onStatus(
                                        status -> status.value() == 404,
                                        (request, response) -> {
                                            throw new PokemonCardNotFoundException(
                                                    serie.id()
                                            );
                                        }
                                )
                                .onStatus(
                                        status -> status.value() == 429
                                                || status.is5xxServerError(),
                                        (request, response) -> {
                                            throw new PokemonTcgApiUnavailableException();
                                        }
                                )
                                .body(TcgdexSerieDetail.class)
                );
            } catch (PokemonCardNotFoundException exception) {
                continue;
            }

            if (detail == null || detail.sets() == null) {
                continue;
            }

            String seriesName = detail.name() != null
                    ? detail.name()
                    : serie.name();

            for (TcgdexSetBrief set : detail.sets()) {
                PokemonSetApiData mapped =
                        tcgdexCardMapper.toSetApiData(set, seriesName);

                if (mapped != null) {
                    sets.add(mapped);
                }
            }
        }

        sets.sort(
                Comparator.comparing(
                        PokemonSetApiData::name,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return new PokemonSetApiResponse(sets);
    }

    private java.net.URI buildCardSearchUri(
            UriBuilder uriBuilder,
            String name,
            String setId,
            String number,
            String rarity,
            int page,
            int pageSize
    ) {
        var builder = uriBuilder
                .path("/cards")
                .queryParam("pagination:page", Math.max(page, 1))
                .queryParam(
                        "pagination:itemsPerPage",
                        Math.max(pageSize, 1)
                );

        if (name != null && !name.isBlank()) {
            builder = builder.queryParam(
                    "name",
                    "like:" + name.trim()
            );
        }

        if (setId != null && !setId.isBlank()
                && number != null && !number.isBlank()) {
            builder = builder.queryParam(
                    "id",
                    "eq:" + setId.trim() + "-" + number.trim()
            );
        } else {
            if (setId != null && !setId.isBlank()) {
                builder = builder.queryParam(
                        "set.id",
                        "eq:" + setId.trim()
                );
            }

            if (number != null && !number.isBlank()) {
                builder = builder.queryParam(
                        "id",
                        "*-" + number.trim()
                );
            }
        }

        if (rarity != null && !rarity.isBlank()) {
            builder = builder.queryParam(
                    "rarity",
                    "like:" + rarity.trim()
            );
        }

        return builder.build();
    }

    private List<PokemonCardApiData> hydrateCards(
            List<TcgdexCardBrief> briefs
    ) {
        return hydrateCards(briefs, null);
    }

    private List<PokemonCardApiData> hydrateCards(
            List<TcgdexCardBrief> briefs,
            TcgdexCardMapper.TcgdexSetDetailContext setContext
    ) {
        if (briefs == null || briefs.isEmpty()) {
            return List.of();
        }

        if (briefs.size() == 1) {
            return List.of(
                    hydrateCard(briefs.getFirst(), setContext)
            );
        }

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(
                             Math.min(
                                     HYDRATION_PARALLELISM,
                                     briefs.size()
                             )
                     )) {
            List<CompletableFuture<PokemonCardApiData>> futures =
                    briefs.stream()
                            .map(brief -> CompletableFuture.supplyAsync(
                                    () -> hydrateCard(brief, setContext),
                                    executor
                            ))
                            .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    private PokemonCardApiData hydrateCard(
            TcgdexCardBrief brief,
            TcgdexCardMapper.TcgdexSetDetailContext setContext
    ) {
        if (brief == null || brief.id() == null || brief.id().isBlank()) {
            return null;
        }

        try {
            PokemonCardApiSingleResponse response = findById(brief.id());

            if (response != null && response.data() != null) {
                return response.data();
            }
        } catch (PokemonCardNotFoundException exception) {
            return tcgdexCardMapper.toCardApiData(brief, setContext);
        } catch (PokemonTcgApiUnavailableException exception) {
            return tcgdexCardMapper.toCardApiData(brief, setContext);
        }

        return tcgdexCardMapper.toCardApiData(brief, setContext);
    }

    private PokemonCardApiResponse toPagedResponse(
            List<PokemonCardApiData> cards,
            int page,
            int pageSize,
            int briefCount
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        boolean hasMore = briefCount >= safePageSize;
        int totalCount = hasMore
                ? safePage * safePageSize + 1
                : (safePage - 1) * safePageSize + briefCount;

        return new PokemonCardApiResponse(
                cards,
                safePage,
                safePageSize,
                cards.size(),
                totalCount
        );
    }

    private <T> T executeWithRetry(Supplier<T> request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
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

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(RETRY_DELAY_MILLISECONDS * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new PokemonTcgApiUnavailableException();
        }
    }
}
