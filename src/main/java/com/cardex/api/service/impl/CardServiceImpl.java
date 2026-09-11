package com.cardex.api.service.impl;

import com.cardex.api.component.CardHistoryRecorder;
import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardFavoriteRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.*;
import com.cardex.api.entity.*;
import com.cardex.api.enumeration.CardCollectionSection;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardHistoryAction;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.exception.CardNotFoundException;
import com.cardex.api.exception.CollectionNotFoundException;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.PokemonTcgApiUnavailableException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.PokemonCardPriceExtractor;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.CardService;
import com.cardex.api.service.ExchangeRateService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.PokemonSetCatalogService;
import com.cardex.api.specification.CardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardex.api.repository.projection.CollectionValueByCollectionProjection;
import com.cardex.api.repository.projection.CollectionValueProjection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final CardHistoryRecorder cardHistoryRecorder;
    private final PokemonCardCatalogService pokemonCardCatalogService;
    private final WishlistCardRepository wishlistCardRepository;
    private final PokemonSetCatalogService pokemonSetCatalogService;
    private final ExchangeRateService exchangeRateService;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "name",
            "collectionName",
            "cardNumber",
            "rarity",
            "quantity",
            "language",
            "condition",
            "createdAt",
            "updatedAt"
    );

    private static final Pattern CARD_NUMBER_PATTERN =
            Pattern.compile("^([A-Za-z]*)(\\d+)(.*)$");

    @Override
    @Transactional
    public CardResponse create(CreateCardRequest request) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        return cardRepository
                .findByUserAndExternalIdAndLanguageAndCondition(
                        authenticatedUser,
                        request.getExternalId(),
                        request.getLanguage(),
                        request.getCondition()
                )
                .map(existingCard ->
                        increaseQuantity(existingCard, request))
                .orElseGet(() ->
                        createNewCard(request, authenticatedUser));
    }

    private CardResponse increaseQuantity(
            CardEntity existingCard,
            CreateCardRequest request
    ) {
        int previousQuantity =
                existingCard.getQuantity();

        int updatedQuantity =
                previousQuantity + request.getQuantity();

        existingCard.setQuantity(updatedQuantity);

        CardEntity updatedCard =
                cardRepository.save(existingCard);

        removeFromWishlistIfPresent(
                updatedCard.getUser(),
                updatedCard.getExternalId()
        );

        cardHistoryRecorder.record(
                updatedCard,
                CardHistoryAction.UPDATED,
                "Quantity changed from "
                        + previousQuantity
                        + " to "
                        + updatedQuantity
                        + "."
        );

        return toPricedResponse(updatedCard);
    }

    private CardResponse createNewCard(
            CreateCardRequest request,
            UserEntity authenticatedUser
    ) {
        PokemonCardCatalogEntity pokemonCard =
                pokemonCardCatalogService.findByExternalId(
                        request.getExternalId()
                );

        CardEntity cardEntity =
                cardMapper.toEntity(request);

        cardEntity.setUser(authenticatedUser);

        cardEntity.setName(
                pokemonCard.getName()
        );

        cardEntity.setCollectionId(
                pokemonCard.getCollectionId()
        );

        cardEntity.setCollectionName(
                pokemonCard.getCollectionName()
        );

        cardEntity.setCollectionTotal(
                pokemonCard.getCollectionTotal()
        );

        cardEntity.setCardNumber(
                pokemonCard.getCardNumber()
        );

        cardEntity.setRarity(
                pokemonCard.getRarity()
        );

        cardEntity.setImageUrl(
                pokemonCard.getImageUrl()
        );

        CardEntity savedCard =
                cardRepository.save(cardEntity);

        removeFromWishlistIfPresent(
                authenticatedUser,
                savedCard.getExternalId()
        );


        cardHistoryRecorder.record(
                savedCard,
                CardHistoryAction.ADDED,
                "Card added to collection."
        );

        return toPricedResponse(savedCard, pokemonCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> findAll(
            int page,
            int size,
            String name,
            String number,
            String collection,
            String rarity,
            CardLanguage language,
            CardCondition condition,
            Boolean favorite,
            String sort
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        Sort cardSort =
                buildSort(sort);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        cardSort
                );

        Specification<CardEntity> specification =
                Specification
                        .where(
                                CardSpecification.userEquals(
                                        authenticatedUser
                                )
                        )
                        .and(
                                CardSpecification.nameContains(
                                        name
                                )
                        )
                        .and(
                                CardSpecification.numberContains(
                                        number
                                )
                        )
                        .and(
                                CardSpecification.collectionContains(
                                        collection
                                )
                        )
                        .and(
                                CardSpecification.rarityContains(
                                        rarity
                                )
                        )
                        .and(
                                CardSpecification.languageEquals(
                                        language
                                )
                        )
                        .and(
                                CardSpecification.conditionEquals(
                                        condition
                                )
                        )
                        .and(
                                CardSpecification.favoriteEquals(
                                        favorite
                                )
                        );

        return enrichPage(
                cardRepository.findAll(
                        specification,
                        pageable
                )
        );
    }

    @Override
    @Transactional
    public CardResponse findById(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity =
                cardRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new CardNotFoundException(id)
                        );

        return toPricedResponse(cardEntity);
    }

    @Override
    @Transactional
    public CardResponse update(
            Long id,
            UpdateCardRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity =
                cardRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new CardNotFoundException(id)
                        );

        String description =
                buildUpdateDescription(
                        cardEntity,
                        request
                );

        cardMapper.updateEntity(
                request,
                cardEntity
        );

        CardEntity updatedCard =
                cardRepository.save(cardEntity);

        if (!description.isBlank()) {
            cardHistoryRecorder.record(
                    updatedCard,
                    CardHistoryAction.UPDATED,
                    description
            );
        }

        return toPricedResponse(updatedCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity =
                cardRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new CardNotFoundException(id)
                        );

        cardHistoryRecorder.record(
                cardEntity,
                CardHistoryAction.REMOVED,
                "Card removed from collection."
        );

        cardRepository.delete(cardEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionSummaryResponse getCollectionSummary() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        long uniqueCards =
                cardRepository.count(
                        CardSpecification.userEquals(
                                authenticatedUser
                        )
                );

        Long totalQuantity =
                cardRepository.sumTotalQuantity(
                        authenticatedUser
                );

        long totalCards =
                totalQuantity != null
                        ? totalQuantity
                        : 0L;

        long differentLanguages =
                cardRepository.countDifferentLanguages(
                        authenticatedUser
                );

        long differentCollections =
                cardRepository.countDifferentCollections(
                        authenticatedUser
                );

        MostOwnedCardResponse mostOwnedCard =
                cardRepository
                        .findFirstByUserOrderByQuantityDescCreatedAtDesc(
                                authenticatedUser
                        )
                        .map(card ->
                                new MostOwnedCardResponse(
                                        card.getName(),
                                        card.getQuantity()
                                )
                        )
                        .orElse(null);

        CollectionValueProjection collectionValue =
                cardRepository.sumCollectionValue(
                        authenticatedUser,
                        exchangeRateService.getUsdToBrlRate(),
                        exchangeRateService.getEurToBrlRate()
                );

        return new CollectionSummaryResponse(
                uniqueCards,
                totalCards,
                differentLanguages,
                differentCollections,
                mostOwnedCard,
                zeroIfNull(
                        collectionValue != null
                                ? collectionValue.getEstimatedValueUsd()
                                : null
                ),
                zeroIfNull(
                        collectionValue != null
                                ? collectionValue.getEstimatedValueEur()
                                : null
                ),
                zeroIfNull(
                        collectionValue != null
                                ? collectionValue.getEstimatedValueBrl()
                                : null
                ),
                collectionValue != null
                        && collectionValue.getPricedCopies() != null
                        ? collectionValue.getPricedCopies()
                        : 0L,
                collectionValue != null
                        && collectionValue.getUnpricedCopies() != null
                        ? collectionValue.getUnpricedCopies()
                        : 0L
        );
    }

    private Sort buildSort(String sort) {
        if (sort == null
                || sort.isBlank()) {
            return Sort.by(
                    Sort.Order
                            .asc("name")
                            .ignoreCase()
            );
        }

        String[] sortParts =
                sort.split(",");

        String property =
                sortParts[0].trim();

        if (!ALLOWED_SORT_PROPERTIES.contains(
                property
        )) {
            property = "name";
        }

        Sort.Direction direction =
                sortParts.length > 1
                        ? Sort.Direction
                        .fromOptionalString(
                                sortParts[1].trim()
                        )
                        .orElse(
                                Sort.Direction.ASC
                        )
                        : Sort.Direction.ASC;

        Sort.Order order =
                new Sort.Order(
                        direction,
                        property
                );

        if ("name".equals(property)) {
            order = order.ignoreCase();
        }

        return Sort.by(order);
    }

    @Override
    @Transactional
    public CardResponse updateFavorite(
            Long id,
            UpdateCardFavoriteRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity =
                cardRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new CardNotFoundException(id)
                        );

        cardEntity.setFavorite(
                request.favorite()
        );

        CardEntity updatedCard =
                cardRepository.save(cardEntity);

        CardHistoryAction action =
                updatedCard.isFavorite()
                        ? CardHistoryAction.FAVORITED
                        : CardHistoryAction.UNFAVORITED;

        String description =
                updatedCard.isFavorite()
                        ? "Card marked as favorite."
                        : "Card removed from favorites.";

        cardHistoryRecorder.record(
                updatedCard,
                action,
                description
        );

        return toPricedResponse(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionAnalyticsResponse getCollectionAnalytics() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CollectionAnalyticsItemResponse> collections =
                cardRepository
                        .findQuantityGroupedByCollection(
                                authenticatedUser
                        )
                        .stream()
                        .map(item ->
                                new CollectionAnalyticsItemResponse(
                                        item.getName(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        List<CollectionAnalyticsItemResponse> languages =
                cardRepository
                        .findQuantityGroupedByLanguage(
                                authenticatedUser
                        )
                        .stream()
                        .map(item ->
                                new CollectionAnalyticsItemResponse(
                                        item.getLanguage().name(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        List<CollectionAnalyticsItemResponse> conditions =
                cardRepository
                        .findQuantityGroupedByCondition(
                                authenticatedUser
                        )
                        .stream()
                        .map(item ->
                                new CollectionAnalyticsItemResponse(
                                        item.getCondition().name(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        List<CollectionAnalyticsItemResponse> rarities =
                cardRepository
                        .findQuantityGroupedByRarity(
                                authenticatedUser
                        )
                        .stream()
                        .map(item ->
                                new CollectionAnalyticsItemResponse(
                                        item.getRarity(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        List<CollectionValueItemResponse> collectionValues =
                cardRepository
                        .findEstimatedValueGroupedByCollection(
                                authenticatedUser,
                                exchangeRateService.getUsdToBrlRate(),
                                exchangeRateService.getEurToBrlRate()
                        )
                        .stream()
                        .map(item ->
                                new CollectionValueItemResponse(
                                        item.getCollectionName(),
                                        zeroIfNull(
                                                item.getEstimatedValueUsd()
                                        ),
                                        zeroIfNull(
                                                item.getEstimatedValueEur()
                                        ),
                                        zeroIfNull(
                                                item.getEstimatedValueBrl()
                                        )
                                )
                        )
                        .toList();

        return new CollectionAnalyticsResponse(
                collections,
                languages,
                conditions,
                rarities,
                collectionValues
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionGoalsResponse getCollectionGoals() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        long uniqueCards =
                cardRepository.count(
                        CardSpecification.userEquals(
                                authenticatedUser
                        )
                );

        Long totalQuantity =
                cardRepository.sumTotalQuantity(
                        authenticatedUser
                );

        long totalCards =
                totalQuantity != null
                        ? totalQuantity
                        : 0L;

        long differentLanguages =
                cardRepository.countDifferentLanguages(
                        authenticatedUser
                );

        long differentCollections =
                cardRepository.countDifferentCollections(
                        authenticatedUser
                );

        boolean hasFavorite =
                cardRepository
                        .existsByUserAndFavoriteTrue(
                                authenticatedUser
                        );

        long rareCards =
                cardRepository.countRareCards(
                        authenticatedUser
                );

        List<CollectionGoalResponse> goals =
                List.of(
                        createGoal(
                                "FIRST_CARD",
                                "First card",
                                "Add your first card to the collection.",
                                totalCards,
                                1
                        ),
                        createGoal(
                                "TEN_CARDS",
                                "10 cards collected",
                                "Reach a total of 10 cards.",
                                totalCards,
                                10
                        ),
                        createGoal(
                                "FIFTY_CARDS",
                                "50 cards collected",
                                "Reach a total of 50 cards.",
                                totalCards,
                                50
                        ),
                        createGoal(
                                "ONE_HUNDRED_CARDS",
                                "100 cards collected",
                                "Reach a total of 100 cards.",
                                totalCards,
                                100
                        ),
                        createGoal(
                                "FIRST_FAVORITE",
                                "First favorite",
                                "Mark your first card as favorite.",
                                hasFavorite ? 1 : 0,
                                1
                        ),
                        createGoal(
                                "FIVE_COLLECTIONS",
                                "5 different collections",
                                "Own cards from at least 5 different collections.",
                                differentCollections,
                                5
                        ),
                        createGoal(
                                "THREE_LANGUAGES",
                                "3 different languages",
                                "Own cards in at least 3 different languages.",
                                differentLanguages,
                                3
                        ),
                        createGoal(
                                "FIRST_RARE_CARD",
                                "First rare card",
                                "Add your first rare card to the collection.",
                                rareCards,
                                1
                        )
                );

        long completedGoals =
                goals
                        .stream()
                        .filter(
                                CollectionGoalResponse::completed
                        )
                        .count();

        return new CollectionGoalsResponse(
                completedGoals,
                goals.size(),
                goals
        );
    }

    private CollectionGoalResponse createGoal(
            String code,
            String title,
            String description,
            long currentValue,
            long targetValue
    ) {
        return new CollectionGoalResponse(
                code,
                title,
                description,
                Math.min(
                        currentValue,
                        targetValue
                ),
                targetValue,
                currentValue >= targetValue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionProgressResponse>
    getCollectionProgress() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        Map<String, CollectionValueByCollectionProjection> valuesByCollection =
                cardRepository
                        .findEstimatedValueGroupedByCollection(
                                authenticatedUser,
                                exchangeRateService.getUsdToBrlRate(),
                                exchangeRateService.getEurToBrlRate()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                CollectionValueByCollectionProjection::getCollectionId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        return cardRepository
                .findCollectionProgress(
                        authenticatedUser
                )
                .stream()
                .map(item -> {
                    long ownedCards =
                            item.getOwnedCards();

                    long totalCards =
                            item.getCollectionTotal();

                    double completionPercentage =
                            totalCards > 0
                                    ? (ownedCards * 100.0)
                                      / totalCards
                                    : 0.0;

                    CollectionValueByCollectionProjection collectionValue =
                            valuesByCollection.get(
                                    item.getCollectionId()
                            );

                    return new CollectionProgressResponse(
                            item.getCollectionId(),
                            item.getCollectionName(),
                            ownedCards,
                            totalCards,
                            Math.round(
                                    completionPercentage
                                            * 100.0
                            ) / 100.0,
                            collectionValue != null
                                    ? zeroIfNull(
                                            collectionValue.getEstimatedValueUsd()
                                    )
                                    : BigDecimal.ZERO.setScale(2),
                            collectionValue != null
                                    ? zeroIfNull(
                                            collectionValue.getEstimatedValueEur()
                                    )
                                    : BigDecimal.ZERO.setScale(2),
                            collectionValue != null
                                    ? zeroIfNull(
                                            collectionValue.getEstimatedValueBrl()
                                    )
                                    : BigDecimal.ZERO.setScale(2)
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public RefreshCardMetadataResponse refreshMetadata() {
        List<CardEntity> cards =
                cardRepository.findAll();

        long processed = 0;
        long updated = 0;

        for (CardEntity card : cards) {
            processed++;

            if (card.getCollectionId() != null
                    && card.getCollectionTotal() != null) {
                continue;
            }

            PokemonCardCatalogEntity catalogCard =
                    pokemonCardCatalogService.findByExternalId(
                            card.getExternalId()
                    );

            card.setCollectionId(
                    catalogCard.getCollectionId()
            );

            card.setCollectionName(
                    catalogCard.getCollectionName()
            );

            card.setCollectionTotal(
                    catalogCard.getCollectionTotal()
            );

            updated++;
        }

        cardRepository.saveAll(cards);

        return new RefreshCardMetadataResponse(
                processed,
                updated
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionDetailsResponse getCollectionDetails(
            String collectionId
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CardEntity> cards =
                cardRepository
                        .findByUserAndCollectionId(
                                authenticatedUser,
                                collectionId
                        );

        if (cards.isEmpty()) {
            throw new CollectionNotFoundException(
                    collectionId
            );
        }

        cards.sort(
                cardNumberComparator()
        );

        CardEntity firstCard =
                cards.get(0);

        long ownedUniqueCards =
                cards
                        .stream()
                        .map(
                                CardEntity::getExternalId
                        )
                        .distinct()
                        .count();

        long totalCards =
                firstCard.getCollectionTotal() != null
                        ? firstCard.getCollectionTotal()
                        : 0;

        double completionPercentage =
                totalCards > 0
                        ? (ownedUniqueCards * 100.0)
                          / totalCards
                        : 0.0;

        List<CollectionOwnedCardResponse> ownedCards =
                cards
                        .stream()
                        .map(card ->
                                new CollectionOwnedCardResponse(
                                        card.getId(),
                                        card.getExternalId(),
                                        card.getName(),
                                        card.getCardNumber(),
                                        card.getRarity(),
                                        card.getImageUrl(),
                                        card.getQuantity(),
                                        card.getLanguage().name(),
                                        card.getCondition().name(),
                                        card.isFavorite()
                                )
                        )
                        .toList();

        return new CollectionDetailsResponse(
                firstCard.getCollectionId(),
                firstCard.getCollectionName(),
                ownedUniqueCards,
                totalCards,
                Math.round(
                        completionPercentage * 100.0
                ) / 100.0,
                ownedCards
        );
    }

    @Override
    @Transactional
    public CollectionChecklistResponse getCollectionChecklist(
            String collectionId
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CardEntity> ownedCards =
                cardRepository
                        .findByUserAndCollectionId(
                                authenticatedUser,
                                collectionId
                        );

        List<WishlistCardEntity> wishlistCards =
                wishlistCardRepository
                        .findAllByUserAndCollectionId(
                                authenticatedUser,
                                collectionId
                        );

        Map<String, CardEntity> ownedCardsByExternalId =
                ownedCards
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        CardEntity::getExternalId,
                                        card -> card,
                                        (first, second) -> first
                                )
                        );

        Map<String, WishlistCardEntity> wishlistCardsByExternalId =
                wishlistCards
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        WishlistCardEntity::getExternalId,
                                        card -> card,
                                        (first, second) -> first
                                )
                        );

        List<PokemonCardCatalogEntity> collectionCards =
                new ArrayList<>(
                        pokemonCardCatalogService
                                .findByCollectionId(
                                        collectionId
                                )
                );

        if (collectionCards.isEmpty()) {
            throw new CollectionNotFoundException(
                    collectionId
            );
        }

        PokemonSetCatalogEntity pokemonSet =
                pokemonSetCatalogService
                        .findByCollectionId(
                                collectionId
                        )
                        .orElseThrow(
                                () ->
                                        new CollectionNotFoundException(
                                                collectionId
                                        )
                        );

        Integer printedTotal =
                pokemonSet.getPrintedTotal();

        collectionCards.sort(
                Comparator.comparing(
                        PokemonCardCatalogEntity::getCardNumber,
                        this::compareCardNumbers
                )
        );

        PokemonCardCatalogEntity firstCatalogCard =
                collectionCards.get(0);

        String collectionName =
                firstCatalogCard.getCollectionName();

        long ownedUniqueCards =
                ownedCardsByExternalId.size();

        long totalCards =
                collectionCards.size();

        double completionPercentage =
                totalCards > 0
                        ? (ownedUniqueCards * 100.0)
                          / totalCards
                        : 0.0;

        BigDecimal estimatedOwnedValueUsd =
                BigDecimal.ZERO.setScale(2);
        BigDecimal estimatedOwnedValueEur =
                BigDecimal.ZERO.setScale(2);
        BigDecimal estimatedOwnedValueBrl =
                BigDecimal.ZERO.setScale(2);
        BigDecimal estimatedMissingValueUsd =
                BigDecimal.ZERO.setScale(2);
        BigDecimal estimatedMissingValueEur =
                BigDecimal.ZERO.setScale(2);
        BigDecimal estimatedMissingValueBrl =
                BigDecimal.ZERO.setScale(2);

        List<CollectionChecklistCardResponse> cards =
                new ArrayList<>();

        for (PokemonCardCatalogEntity catalogCard : collectionCards) {
            CardEntity ownedCard =
                    ownedCardsByExternalId.get(
                            catalogCard.getExternalId()
                    );

            WishlistCardEntity wishlistCard =
                    wishlistCardsByExternalId.get(
                            catalogCard.getExternalId()
                    );

            BigDecimal marketPriceBrl =
                    exchangeRateService.toBrl(
                            catalogCard.getMarketPriceUsd(),
                            catalogCard.getMarketPriceEur()
                    );

            if (ownedCard != null) {
                estimatedOwnedValueUsd =
                        addAmount(
                                estimatedOwnedValueUsd,
                                PokemonCardPriceExtractor.multiply(
                                        catalogCard.getMarketPriceUsd(),
                                        ownedCard.getQuantity()
                                )
                        );

                estimatedOwnedValueEur =
                        addAmount(
                                estimatedOwnedValueEur,
                                PokemonCardPriceExtractor.multiply(
                                        catalogCard.getMarketPriceEur(),
                                        ownedCard.getQuantity()
                                )
                        );

                estimatedOwnedValueBrl =
                        addAmount(
                                estimatedOwnedValueBrl,
                                PokemonCardPriceExtractor.multiply(
                                        marketPriceBrl,
                                        ownedCard.getQuantity()
                                )
                        );
            } else {
                estimatedMissingValueUsd =
                        addAmount(
                                estimatedMissingValueUsd,
                                PokemonCardPriceExtractor.multiply(
                                        catalogCard.getMarketPriceUsd(),
                                        1
                                )
                        );

                estimatedMissingValueEur =
                        addAmount(
                                estimatedMissingValueEur,
                                PokemonCardPriceExtractor.multiply(
                                        catalogCard.getMarketPriceEur(),
                                        1
                                )
                        );

                estimatedMissingValueBrl =
                        addAmount(
                                estimatedMissingValueBrl,
                                PokemonCardPriceExtractor.multiply(
                                        marketPriceBrl,
                                        1
                                )
                        );
            }

            cards.add(
                    new CollectionChecklistCardResponse(
                            catalogCard.getExternalId(),
                            catalogCard.getName(),
                            catalogCard.getCardNumber(),
                            catalogCard.getRarity(),
                            catalogCard.getImageUrl(),
                            ownedCard != null,
                            ownedCard != null
                                    ? ownedCard.getId()
                                    : null,
                            wishlistCard != null,
                            wishlistCard != null
                                    ? wishlistCard.getId()
                                    : null,
                            wishlistCard != null
                                    ? wishlistCard.getPriority()
                                    : null,
                            determineCardCollectionSection(
                                    catalogCard.getCardNumber(),
                                    printedTotal
                            ),
                            catalogCard.getMarketPriceUsd(),
                            catalogCard.getMarketPriceEur(),
                            marketPriceBrl
                    )
            );
        }

        long numberedCards =
                cards.stream()
                        .filter(card ->
                                card.section()
                                        == CardCollectionSection.NUMBERED
                        )
                        .count();

        long additionalCards =
                cards.stream()
                        .filter(card ->
                                card.section()
                                        == CardCollectionSection.ADDITIONAL
                        )
                        .count();

        long ownedNumberedCards =
                cards.stream()
                        .filter(CollectionChecklistCardResponse::owned)
                        .filter(card ->
                                card.section()
                                        == CardCollectionSection.NUMBERED
                        )
                        .count();

        long ownedAdditionalCards =
                cards.stream()
                        .filter(CollectionChecklistCardResponse::owned)
                        .filter(card ->
                                card.section()
                                        == CardCollectionSection.ADDITIONAL
                        )
                        .count();

        return new CollectionChecklistResponse(
                collectionId,
                collectionName,
                ownedUniqueCards,
                totalCards,
                Math.round(
                        completionPercentage * 100.0
                ) / 100.0,
                ownedNumberedCards,
                numberedCards,
                ownedAdditionalCards,
                additionalCards,
                estimatedOwnedValueUsd,
                estimatedOwnedValueEur,
                estimatedOwnedValueBrl,
                estimatedMissingValueUsd,
                estimatedMissingValueEur,
                estimatedMissingValueBrl,
                cards
        );
    }

    private CardCollectionSection determineCardCollectionSection(
            String cardNumber,
            Integer printedTotal
    ) {
        if (cardNumber == null
                || cardNumber.isBlank()
                || printedTotal == null
                || printedTotal <= 0) {
            return CardCollectionSection.ADDITIONAL;
        }

        try {
            long number =
                    Long.parseLong(
                            cardNumber.trim()
                    );

            return number <= printedTotal
                    ? CardCollectionSection.NUMBERED
                    : CardCollectionSection.ADDITIONAL;
        } catch (NumberFormatException exception) {
            return CardCollectionSection.ADDITIONAL;
        }
    }

    private Comparator<CardEntity> cardNumberComparator() {
        return Comparator.comparing(
                CardEntity::getCardNumber,
                this::compareCardNumbers
        );
    }

    private int compareCardNumbers(
            String first,
            String second
    ) {
        if (first == null
                && second == null) {
            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        Matcher firstMatcher =
                CARD_NUMBER_PATTERN.matcher(
                        first
                );

        Matcher secondMatcher =
                CARD_NUMBER_PATTERN.matcher(
                        second
                );

        if (!firstMatcher.matches()
                || !secondMatcher.matches()) {
            return first.compareToIgnoreCase(
                    second
            );
        }

        String firstPrefix =
                firstMatcher.group(1);

        String secondPrefix =
                secondMatcher.group(1);

        int prefixComparison =
                firstPrefix.compareToIgnoreCase(
                        secondPrefix
                );

        if (prefixComparison != 0) {
            return prefixComparison;
        }

        long firstNumber =
                Long.parseLong(
                        firstMatcher.group(2)
                );

        long secondNumber =
                Long.parseLong(
                        secondMatcher.group(2)
                );

        int numberComparison =
                Long.compare(
                        firstNumber,
                        secondNumber
                );

        if (numberComparison != 0) {
            return numberComparison;
        }

        String firstSuffix =
                firstMatcher.group(3);

        String secondSuffix =
                secondMatcher.group(3);

        return firstSuffix.compareToIgnoreCase(
                secondSuffix
        );
    }

    private String buildUpdateDescription(
            CardEntity card,
            UpdateCardRequest request
    ) {
        List<String> changes =
                new ArrayList<>();

        if (!Objects.equals(
                card.getQuantity(),
                request.getQuantity()
        )) {
            changes.add(
                    "Quantity changed from "
                            + card.getQuantity()
                            + " to "
                            + request.getQuantity()
                            + "."
            );
        }

        if (!Objects.equals(
                card.getLanguage(),
                request.getLanguage()
        )) {
            changes.add(
                    "Language changed from "
                            + card.getLanguage()
                            + " to "
                            + request.getLanguage()
                            + "."
            );
        }

        if (!Objects.equals(
                card.getCondition(),
                request.getCondition()
        )) {
            changes.add(
                    "Condition changed from "
                            + card.getCondition()
                            + " to "
                            + request.getCondition()
                            + "."
            );
        }

        if (!Objects.equals(
                card.getNotes(),
                request.getNotes()
        )) {
            changes.add(
                    "Notes updated."
            );
        }

        return String.join(
                " ",
                changes
        );
    }

    private Page<CardResponse> enrichPage(
            Page<CardEntity> page
    ) {
        List<String> externalIds =
                page.getContent()
                        .stream()
                        .map(CardEntity::getExternalId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<String, PokemonCardCatalogEntity> catalogByExternalId =
                Optional.ofNullable(
                                pokemonCardCatalogService
                                        .findAllByExternalIdIn(externalIds)
                        )
                        .orElse(List.of())
                        .stream()
                        .collect(Collectors.toMap(
                                PokemonCardCatalogEntity::getExternalId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        return page.map(card ->
                PokemonCardPriceExtractor.enrich(
                        cardMapper.toResponse(card),
                        catalogByExternalId.get(
                                card.getExternalId()
                        ),
                        card.getQuantity(),
                        exchangeRateService::toBrl
                )
        );
    }

    private CardResponse toPricedResponse(
            CardEntity card
    ) {
        if (card.getExternalId() == null) {
            return cardMapper.toResponse(card);
        }

        try {
            PokemonCardCatalogEntity catalog =
                    pokemonCardCatalogService.findByExternalId(
                            card.getExternalId()
                    );

            return toPricedResponse(card, catalog);
        } catch (PokemonCardNotFoundException
                 | PokemonTcgApiUnavailableException exception) {
            return cardMapper.toResponse(card);
        }
    }

    private CardResponse toPricedResponse(
            CardEntity card,
            PokemonCardCatalogEntity catalog
    ) {
        return PokemonCardPriceExtractor.enrich(
                cardMapper.toResponse(card),
                catalog,
                card.getQuantity(),
                exchangeRateService::toBrl
        );
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null
                ? value
                : BigDecimal.ZERO.setScale(2);
    }

    private BigDecimal addAmount(
            BigDecimal total,
            BigDecimal value
    ) {
        if (value == null) {
            return total;
        }

        return total.add(value);
    }

    private void removeFromWishlistIfPresent(
            UserEntity user,
            String externalId
    ) {
        wishlistCardRepository
                .findByUserAndExternalId(
                        user,
                        externalId
                )
                .ifPresent(
                        wishlistCardRepository::delete
                );
    }
}