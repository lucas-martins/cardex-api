package com.cardex.api.service.impl;

import com.cardex.api.deck.DeckCardResolver;
import com.cardex.api.deck.ResolvedDeckCard;
import com.cardex.api.dto.deck.AddDeckCardRequest;
import com.cardex.api.dto.deck.CreateDeckRequest;
import com.cardex.api.dto.deck.DeckCardInput;
import com.cardex.api.dto.deck.DeckCardResponse;
import com.cardex.api.dto.deck.DeckParseResponse;
import com.cardex.api.dto.deck.DeckResponse;
import com.cardex.api.dto.deck.DeckSummaryResponse;
import com.cardex.api.dto.deck.ParseDeckListRequest;
import com.cardex.api.dto.deck.ReplaceDeckCardsRequest;
import com.cardex.api.dto.deck.UnresolvedDeckCardResponse;
import com.cardex.api.dto.deck.UpdateDeckRequest;
import com.cardex.api.entity.DeckCardEntity;
import com.cardex.api.entity.DeckEntity;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.exception.DeckCardNotFoundException;
import com.cardex.api.exception.DeckNotFoundException;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.DeckCardRepository;
import com.cardex.api.repository.DeckRepository;
import com.cardex.api.repository.projection.ExternalIdQuantityProjection;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.DeckService;
import com.cardex.api.service.PokemonCardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final DeckCardRepository deckCardRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final DeckCardResolver deckCardResolver;
    private final PokemonCardCatalogService pokemonCardCatalogService;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public DeckParseResponse parse(ParseDeckListRequest request) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();

        List<ResolvedDeckCard> resolved =
                deckCardResolver.resolveList(request.listText(), user);

        List<DeckCardResponse> resolvedCards = new ArrayList<>();
        List<UnresolvedDeckCardResponse> unresolvedCards = new ArrayList<>();

        for (ResolvedDeckCard card : resolved) {
            if (card.resolved()) {
                resolvedCards.add(toCardResponse(card, null));
            } else {
                unresolvedCards.add(
                        new UnresolvedDeckCardResponse(
                                card.lineNumber(),
                                card.rawLine(),
                                card.section(),
                                card.quantity(),
                                card.requestedName(),
                                card.setAbbreviation(),
                                card.cardNumber()
                        )
                );
            }
        }

        return new DeckParseResponse(
                resolved.size(),
                resolvedCards.size(),
                unresolvedCards.size(),
                mergeResolvedCards(resolvedCards),
                unresolvedCards
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeckSummaryResponse> findAll() {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();

        return deckRepository.findAllByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeckResponse findById(Long id) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(id, user);
        return toResponse(deck, user);
    }

    @Override
    @Transactional
    public DeckResponse create(CreateDeckRequest request) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();

        DeckEntity deck = DeckEntity.builder()
                .user(user)
                .name(request.name().trim())
                .notes(blankToNull(request.notes()))
                .build();

        applyCards(deck, request.cards());

        DeckEntity saved = deckRepository.save(deck);
        return toResponse(saved, user);
    }

    @Override
    @Transactional
    public DeckResponse update(Long id, UpdateDeckRequest request) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(id, user);

        if (request.name() != null && !request.name().isBlank()) {
            deck.setName(request.name().trim());
        }

        if (request.notes() != null) {
            deck.setNotes(blankToNull(request.notes()));
        }

        return toResponse(deckRepository.save(deck), user);
    }

    @Override
    @Transactional
    public DeckResponse replaceCards(
            Long id,
            ReplaceDeckCardsRequest request
    ) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(id, user);

        deck.clearCards();
        applyCards(deck, request.cards());

        return toResponse(deckRepository.save(deck), user);
    }

    @Override
    @Transactional
    public DeckResponse addCard(Long id, AddDeckCardRequest request) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(id, user);

        PokemonCardCatalogEntity catalog =
                pokemonCardCatalogService.findByExternalId(
                        request.externalId()
                );

        DeckCardEntity existing = deck.getCards().stream()
                .filter(card ->
                        card.getExternalId().equals(catalog.getExternalId())
                )
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(
                    existing.getQuantity() + request.quantity()
            );
            existing.setSection(request.section());
        } else {
            deck.addCard(buildCardEntity(catalog, request.section(), request.quantity()));
        }

        return toResponse(deckRepository.save(deck), user);
    }

    @Override
    @Transactional
    public DeckResponse removeCard(Long deckId, Long cardId) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(deckId, user);

        DeckCardEntity card = deckCardRepository
                .findByIdAndDeck(cardId, deck)
                .orElseThrow(() -> new DeckCardNotFoundException(cardId));

        deck.getCards().remove(card);
        card.setDeck(null);

        return toResponse(deckRepository.save(deck), user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        DeckEntity deck = getDeck(id, user);
        deckRepository.delete(deck);
    }

    private void applyCards(DeckEntity deck, List<DeckCardInput> cards) {
        Map<String, DeckCardInput> uniqueCards = new LinkedHashMap<>();

        for (DeckCardInput card : cards) {
            uniqueCards.merge(
                    card.externalId(),
                    card,
                    (existing, incoming) -> new DeckCardInput(
                            existing.externalId(),
                            incoming.section(),
                            existing.quantity() + incoming.quantity()
                    )
            );
        }

        for (DeckCardInput card : uniqueCards.values()) {
            PokemonCardCatalogEntity catalog =
                    pokemonCardCatalogService.findByExternalId(
                            card.externalId()
                    );

            deck.addCard(
                    buildCardEntity(
                            catalog,
                            card.section(),
                            card.quantity()
                    )
            );
        }
    }

    private DeckCardEntity buildCardEntity(
            PokemonCardCatalogEntity catalog,
            com.cardex.api.enumeration.DeckSection section,
            int quantity
    ) {
        return DeckCardEntity.builder()
                .externalId(catalog.getExternalId())
                .name(catalog.getName())
                .cardNumber(catalog.getCardNumber())
                .collectionId(catalog.getCollectionId())
                .collectionName(catalog.getCollectionName())
                .imageUrl(catalog.getImageUrl())
                .section(section)
                .quantity(quantity)
                .build();
    }

    private DeckEntity getDeck(Long id, UserEntity user) {
        return deckRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new DeckNotFoundException(id));
    }

    private DeckSummaryResponse toSummary(DeckEntity deck) {
        int totalCards = deck.getCards().stream()
                .mapToInt(DeckCardEntity::getQuantity)
                .sum();

        return new DeckSummaryResponse(
                deck.getId(),
                deck.getName(),
                deck.getNotes(),
                totalCards,
                deck.getCards().size(),
                deck.getCreatedAt(),
                deck.getUpdatedAt()
        );
    }

    private DeckResponse toResponse(DeckEntity deck, UserEntity user) {
        Map<String, Integer> owned = ownedQuantities(deck, user);

        List<DeckCardResponse> cards = deck.getCards().stream()
                .map(card -> new DeckCardResponse(
                        card.getId(),
                        card.getExternalId(),
                        card.getName(),
                        card.getCardNumber(),
                        card.getCollectionId(),
                        card.getCollectionName(),
                        card.getImageUrl(),
                        card.getSection(),
                        card.getQuantity(),
                        owned.getOrDefault(card.getExternalId(), 0)
                ))
                .toList();

        int totalCards = cards.stream()
                .mapToInt(DeckCardResponse::quantity)
                .sum();

        return new DeckResponse(
                deck.getId(),
                deck.getName(),
                deck.getNotes(),
                totalCards,
                cards.size(),
                cards,
                deck.getCreatedAt(),
                deck.getUpdatedAt()
        );
    }

    private Map<String, Integer> ownedQuantities(
            DeckEntity deck,
            UserEntity user
    ) {
        List<String> externalIds = deck.getCards().stream()
                .map(DeckCardEntity::getExternalId)
                .distinct()
                .toList();

        Map<String, Integer> owned = new HashMap<>();

        if (externalIds.isEmpty()) {
            return owned;
        }

        for (ExternalIdQuantityProjection projection :
                cardRepository.sumQuantityByUserAndExternalIdIn(
                        user,
                        externalIds
                )) {
            owned.put(
                    projection.getExternalId(),
                    projection.getQuantity() == null
                            ? 0
                            : projection.getQuantity().intValue()
            );
        }

        return owned;
    }

    private DeckCardResponse toCardResponse(
            ResolvedDeckCard card,
            Long id
    ) {
        return new DeckCardResponse(
                id,
                card.externalId(),
                card.name(),
                card.cardNumber(),
                card.collectionId(),
                card.collectionName(),
                card.imageUrl(),
                card.section(),
                card.quantity(),
                card.ownedQuantity()
        );
    }

    private List<DeckCardResponse> mergeResolvedCards(
            List<DeckCardResponse> cards
    ) {
        Map<String, DeckCardResponse> merged = new LinkedHashMap<>();

        for (DeckCardResponse card : cards) {
            merged.merge(
                    card.externalId(),
                    card,
                    (existing, incoming) -> new DeckCardResponse(
                            existing.id(),
                            existing.externalId(),
                            existing.name(),
                            existing.cardNumber(),
                            existing.collectionId(),
                            existing.collectionName(),
                            existing.imageUrl(),
                            incoming.section(),
                            existing.quantity() + incoming.quantity(),
                            Math.max(
                                    existing.ownedQuantity(),
                                    incoming.ownedQuantity()
                            )
                    )
            );
        }

        return new ArrayList<>(merged.values());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
