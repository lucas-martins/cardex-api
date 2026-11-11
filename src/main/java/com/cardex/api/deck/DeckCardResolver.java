package com.cardex.api.deck;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.PokemonSetCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.PokemonCardCatalogRepository;
import com.cardex.api.repository.PokemonSetCatalogRepository;
import com.cardex.api.repository.projection.ExternalIdQuantityProjection;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.PokemonSetCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeckCardResolver {

    private final DeckListParser deckListParser;
    private final PokemonSetCatalogRepository pokemonSetCatalogRepository;
    private final PokemonCardCatalogRepository pokemonCardCatalogRepository;
    private final PokemonCardCatalogService pokemonCardCatalogService;
    private final PokemonSetCatalogService pokemonSetCatalogService;
    private final PokemonTcgClient pokemonTcgClient;
    private final CardRepository cardRepository;

    public List<ResolvedDeckCard> resolveList(
            String listText,
            UserEntity user
    ) {
        ensureSetAbbreviationsAvailable();

        List<DeckListLine> lines = deckListParser.parse(listText);
        List<ResolvedDeckCard> resolvedWithoutOwned = new ArrayList<>();

        for (DeckListLine line : lines) {
            resolvedWithoutOwned.add(resolveLine(line));
        }

        return attachOwnedQuantities(resolvedWithoutOwned, user);
    }

    public ResolvedDeckCard resolveManualCard(
            String externalId,
            int quantity,
            com.cardex.api.enumeration.DeckSection section,
            UserEntity user
    ) {
        PokemonCardCatalogEntity catalog =
                pokemonCardCatalogService.findByExternalId(externalId);

        ResolvedDeckCard resolved = new ResolvedDeckCard(
                true,
                0,
                null,
                section,
                quantity,
                catalog.getName(),
                null,
                catalog.getCardNumber(),
                catalog.getExternalId(),
                catalog.getName(),
                catalog.getCollectionId(),
                catalog.getCollectionName(),
                catalog.getImageUrl(),
                0
        );

        return attachOwnedQuantities(List.of(resolved), user).getFirst();
    }

    private ResolvedDeckCard resolveLine(DeckListLine line) {
        Optional<PokemonSetCatalogEntity> set =
                pokemonSetCatalogRepository
                        .findFirstByAbbreviationIgnoreCase(
                                line.setAbbreviation()
                        );

        if (set.isPresent()) {
            Optional<PokemonCardCatalogEntity> byNumber =
                    findCatalogCard(
                            set.get().getCollectionId(),
                            line.cardNumber()
                    );

            if (byNumber.isPresent()) {
                return toResolved(line, byNumber.get());
            }

            Optional<PokemonCardCatalogEntity> fromApi =
                    loadFromApi(
                            set.get().getCollectionId()
                                    + "-"
                                    + line.cardNumber()
                    );

            if (fromApi.isPresent()) {
                return toResolved(line, fromApi.get());
            }

            Optional<PokemonCardCatalogEntity> padded =
                    loadFromApi(
                            set.get().getCollectionId()
                                    + "-"
                                    + padNumber(line.cardNumber())
                    );

            if (padded.isPresent()) {
                return toResolved(line, padded.get());
            }
        }

        Optional<PokemonCardCatalogEntity> byName =
                findByNameFallback(line);

        if (byName.isPresent()) {
            return toResolved(line, byName.get());
        }

        return unresolved(line);
    }

    private Optional<PokemonCardCatalogEntity> findCatalogCard(
            String collectionId,
            String cardNumber
    ) {
        Optional<PokemonCardCatalogEntity> exact =
                pokemonCardCatalogRepository
                        .findFirstByCollectionIdAndCardNumberIgnoreCase(
                                collectionId,
                                cardNumber
                        );

        if (exact.isPresent()) {
            return exact;
        }

        return pokemonCardCatalogRepository
                .findFirstByCollectionIdAndCardNumberIgnoreCase(
                        collectionId,
                        padNumber(cardNumber)
                );
    }

    private Optional<PokemonCardCatalogEntity> loadFromApi(
            String externalId
    ) {
        try {
            return Optional.of(
                    pokemonCardCatalogService.findByExternalId(externalId)
            );
        } catch (PokemonCardNotFoundException
                 | PokemonTcgApiUnavailableException exception) {
            return Optional.empty();
        }
    }

    private Optional<PokemonCardCatalogEntity> findByNameFallback(
            DeckListLine line
    ) {
        List<PokemonCardCatalogEntity> exactNameMatches =
                pokemonCardCatalogRepository.findByNameIgnoreCase(
                        line.name()
                );

        List<PokemonCardCatalogEntity> hyphenNameMatches =
                exactNameMatches.isEmpty()
                        ? pokemonCardCatalogRepository.findByNameIgnoreCase(
                        line.name().replace(" ", "-")
                )
                        : List.of();

        List<PokemonCardCatalogEntity> byName = exactNameMatches.isEmpty()
                ? hyphenNameMatches
                : exactNameMatches;

        if (!byName.isEmpty()) {
            final List<PokemonCardCatalogEntity> catalogMatches = byName;

            Optional<PokemonCardCatalogEntity> withSet =
                    catalogMatches.stream()
                            .filter(card ->
                                    numbersMatch(
                                            card.getCardNumber(),
                                            line.cardNumber()
                                    )
                            )
                            .filter(card ->
                                    setMatchesAbbreviation(
                                            card.getCollectionId(),
                                            line.setAbbreviation()
                                    )
                            )
                            .findFirst();

            if (withSet.isPresent()) {
                return withSet;
            }
        }

        return findAndCacheFromApi(line);
    }

    private Optional<PokemonCardCatalogEntity> findAndCacheFromApi(
            DeckListLine line
    ) {
        try {
            var response = pokemonTcgClient.searchCards(
                    line.name(),
                    null,
                    line.cardNumber(),
                    null,
                    1,
                    20
            );

            if (response == null || response.data() == null) {
                return Optional.empty();
            }

            pokemonCardCatalogService.cacheCards(response.data());

            Map<String, String> abbreviationCache = new HashMap<>();

            for (PokemonCardApiData card : response.data()) {
                if (card.set() == null
                        || !numbersMatch(card.number(), line.cardNumber())) {
                    continue;
                }

                if (matchesAbbreviation(
                        card,
                        line.setAbbreviation(),
                        abbreviationCache
                )) {
                    return loadFromApi(card.id());
                }
            }

            for (PokemonCardApiData card : response.data()) {
                if (numbersMatch(card.number(), line.cardNumber())
                        && namesLooselyMatch(card.name(), line.name())) {
                    return loadFromApi(card.id());
                }
            }
        } catch (PokemonTcgApiUnavailableException exception) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    private boolean namesLooselyMatch(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        String normalizedLeft = left.replace("-", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
        String normalizedRight = right.replace("-", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();

        return normalizedLeft.equals(normalizedRight);
    }

    private boolean matchesAbbreviation(
            PokemonCardApiData card,
            String abbreviation,
            Map<String, String> abbreviationCache
    ) {
        if (card.set() == null) {
            return false;
        }

        if (card.set().abbreviation() != null
                && card.set().abbreviation()
                .equalsIgnoreCase(abbreviation)) {
            return true;
        }

        if (setMatchesAbbreviation(card.set().id(), abbreviation)) {
            return true;
        }

        String liveAbbreviation = abbreviationCache.computeIfAbsent(
                card.set().id(),
                pokemonTcgClient::findSetAbbreviation
        );

        if (liveAbbreviation != null
                && liveAbbreviation.equalsIgnoreCase(abbreviation)) {
            persistAbbreviation(card.set().id(), liveAbbreviation);
            return true;
        }

        return false;
    }

    private void persistAbbreviation(
            String collectionId,
            String abbreviation
    ) {
        pokemonSetCatalogRepository
                .findByCollectionId(collectionId)
                .ifPresent(set -> {
                    if (set.getAbbreviation() == null
                            || set.getAbbreviation().isBlank()) {
                        set.setAbbreviation(abbreviation);
                        pokemonSetCatalogRepository.save(set);
                    }
                });
    }

    private boolean setMatchesAbbreviation(
            String collectionId,
            String abbreviation
    ) {
        if (collectionId == null || abbreviation == null) {
            return false;
        }

        return pokemonSetCatalogRepository
                .findByCollectionId(collectionId)
                .map(PokemonSetCatalogEntity::getAbbreviation)
                .filter(value -> value != null
                        && value.equalsIgnoreCase(abbreviation))
                .isPresent();
    }

    private boolean numbersMatch(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        String normalizedLeft = normalizeNumber(left);
        String normalizedRight = normalizeNumber(right);

        return normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private String normalizeNumber(String value) {
        String trimmed = value.trim();

        if (trimmed.matches("\\d+")) {
            return String.valueOf(Integer.parseInt(trimmed));
        }

        return trimmed;
    }

    private String padNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d+")) {
            return cardNumber;
        }

        int number = Integer.parseInt(cardNumber);

        if (number < 10) {
            return String.format("%03d", number);
        }

        if (number < 100) {
            return String.format("%03d", number);
        }

        return cardNumber;
    }

    private ResolvedDeckCard toResolved(
            DeckListLine line,
            PokemonCardCatalogEntity catalog
    ) {
        return new ResolvedDeckCard(
                true,
                line.lineNumber(),
                line.rawLine(),
                line.section(),
                line.quantity(),
                line.name(),
                line.setAbbreviation(),
                line.cardNumber(),
                catalog.getExternalId(),
                catalog.getName(),
                catalog.getCollectionId(),
                catalog.getCollectionName(),
                catalog.getImageUrl(),
                0
        );
    }

    private ResolvedDeckCard unresolved(DeckListLine line) {
        return new ResolvedDeckCard(
                false,
                line.lineNumber(),
                line.rawLine(),
                line.section(),
                line.quantity(),
                line.name(),
                line.setAbbreviation(),
                line.cardNumber(),
                null,
                null,
                null,
                null,
                null,
                0
        );
    }

    private List<ResolvedDeckCard> attachOwnedQuantities(
            List<ResolvedDeckCard> cards,
            UserEntity user
    ) {
        List<String> externalIds = cards.stream()
                .filter(ResolvedDeckCard::resolved)
                .map(ResolvedDeckCard::externalId)
                .distinct()
                .toList();

        Map<String, Integer> ownedByExternalId = new HashMap<>();

        if (!externalIds.isEmpty()) {
            for (ExternalIdQuantityProjection projection :
                    cardRepository.sumQuantityByUserAndExternalIdIn(
                            user,
                            externalIds
                    )) {
                ownedByExternalId.put(
                        projection.getExternalId(),
                        projection.getQuantity() == null
                                ? 0
                                : projection.getQuantity().intValue()
                );
            }
        }

        return cards.stream()
                .map(card -> new ResolvedDeckCard(
                        card.resolved(),
                        card.lineNumber(),
                        card.rawLine(),
                        card.section(),
                        card.quantity(),
                        card.requestedName(),
                        card.setAbbreviation(),
                        card.cardNumber(),
                        card.externalId(),
                        card.name(),
                        card.collectionId(),
                        card.collectionName(),
                        card.imageUrl(),
                        card.resolved()
                                ? ownedByExternalId.getOrDefault(
                                card.externalId(),
                                0
                        )
                                : 0
                ))
                .toList();
    }

    private void ensureSetAbbreviationsAvailable() {
        boolean missingAbbreviations = pokemonSetCatalogRepository
                .findAllByOrderByNameAsc()
                .stream()
                .anyMatch(set -> set.getAbbreviation() == null);

        if (missingAbbreviations) {
            pokemonSetCatalogService.enrichMissingAbbreviations();
        }
    }
}
