package com.cardex.api.service.impl;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardFavoriteRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.*;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.exception.CollectionNotFoundException;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.exception.CardNotFoundException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.CardService;
import com.cardex.api.specification.CardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final PokemonTcgClient pokemonTcgClient;
    private final AuthenticatedUserService authenticatedUserService;
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
                .map(existingCard -> increaseQuantity(existingCard, request))
                .orElseGet(() -> createNewCard(request, authenticatedUser));
    }

    private CardResponse increaseQuantity(
            CardEntity existingCard,
            CreateCardRequest request
    ) {
        int updatedQuantity =
                existingCard.getQuantity() + request.getQuantity();

        existingCard.setQuantity(updatedQuantity);

        CardEntity updatedCard = cardRepository.save(existingCard);

        return cardMapper.toResponse(updatedCard);
    }

    private CardResponse createNewCard(CreateCardRequest request, UserEntity authenticatedUser) {
        PokemonCardApiSingleResponse apiResponse =
                pokemonTcgClient.findById(request.getExternalId());

        if (apiResponse == null || apiResponse.data() == null) {
            throw new PokemonCardNotFoundException(request.getExternalId());
        }

        PokemonCardApiData pokemonCard = apiResponse.data();

        CardEntity cardEntity = cardMapper.toEntity(request);
        cardEntity.setUser(authenticatedUser);

        cardEntity.setName(pokemonCard.name());
        if (pokemonCard.set() != null) {
            cardEntity.setCollectionId(pokemonCard.set().id());
            cardEntity.setCollectionName(pokemonCard.set().name());
            cardEntity.setCollectionTotal(pokemonCard.set().total());
        } else {
            cardEntity.setCollectionId(null);
            cardEntity.setCollectionName(null);
            cardEntity.setCollectionTotal(null);
        }
        cardEntity.setCardNumber(pokemonCard.number());
        cardEntity.setRarity(pokemonCard.rarity());
        cardEntity.setImageUrl(
                pokemonCard.images() != null
                        ? pokemonCard.images().large()
                        : null
        );

        CardEntity savedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> findAll(
            int page,
            int size,
            String name,
            CardLanguage language,
            CardCondition condition,
            Boolean favorite,
            String sort
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        Sort cardSort = buildSort(sort);

        Pageable pageable = PageRequest.of(page, size, cardSort);

        Specification<CardEntity> specification =
                Specification
                        .where(CardSpecification.userEquals(authenticatedUser))
                        .and(CardSpecification.nameContains(name))
                        .and(CardSpecification.languageEquals(language))
                        .and(CardSpecification.conditionEquals(condition))
                        .and(CardSpecification.favoriteEquals(favorite));

        return cardRepository
                .findAll(specification, pageable)
                .map(cardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse findById(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity = cardRepository
                .findByIdAndUser(id, authenticatedUser)
                .orElseThrow(() -> new CardNotFoundException(id));

        return cardMapper.toResponse(cardEntity);
    }

    @Override
    @Transactional
    public CardResponse update(Long id, UpdateCardRequest request) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity = cardRepository
                .findByIdAndUser(id, authenticatedUser)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardMapper.updateEntity(request, cardEntity);

        CardEntity updatedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(updatedCard);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CardEntity cardEntity = cardRepository
                .findByIdAndUser(id, authenticatedUser)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.delete(cardEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionSummaryResponse getCollectionSummary() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        long uniqueCards = cardRepository.count(
                CardSpecification.userEquals(authenticatedUser)
        );

        Long totalQuantity =
                cardRepository.sumTotalQuantity(authenticatedUser);

        long totalCards =
                totalQuantity != null ? totalQuantity : 0L;

        long differentLanguages =
                cardRepository.countDifferentLanguages(authenticatedUser);

        long differentCollections =
                cardRepository.countDifferentCollections(authenticatedUser);

        MostOwnedCardResponse mostOwnedCard = cardRepository
                .findFirstByUserOrderByQuantityDescCreatedAtDesc(
                        authenticatedUser
                )
                .map(card -> new MostOwnedCardResponse(
                        card.getName(),
                        card.getQuantity()
                ))
                .orElse(null);

        return new CollectionSummaryResponse(
                uniqueCards,
                totalCards,
                differentLanguages,
                differentCollections,
                mostOwnedCard
        );
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(
                    Sort.Order.asc("name").ignoreCase()
            );
        }

        String[] sortParts = sort.split(",");

        String property = sortParts[0].trim();

        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            property = "name";
        }

        Sort.Direction direction =
                sortParts.length > 1
                        ? Sort.Direction.fromOptionalString(
                        sortParts[1].trim()
                ).orElse(Sort.Direction.ASC)
                        : Sort.Direction.ASC;

        Sort.Order order = new Sort.Order(direction, property);

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

        CardEntity cardEntity = cardRepository
                .findByIdAndUser(id, authenticatedUser)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardEntity.setFavorite(request.favorite());

        CardEntity updatedCard = cardRepository.save(cardEntity);

        return cardMapper.toResponse(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionAnalyticsResponse getCollectionAnalytics() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CollectionAnalyticsItemResponse> collections =
                cardRepository
                        .findQuantityGroupedByCollection(authenticatedUser)
                        .stream()
                        .map(item -> new CollectionAnalyticsItemResponse(
                                item.getName(),
                                item.getQuantity()
                        ))
                        .toList();

        List<CollectionAnalyticsItemResponse> languages =
                cardRepository
                        .findQuantityGroupedByLanguage(authenticatedUser)
                        .stream()
                        .map(item -> new CollectionAnalyticsItemResponse(
                                item.getLanguage().name(),
                                item.getQuantity()
                        ))
                        .toList();

        List<CollectionAnalyticsItemResponse> conditions =
                cardRepository
                        .findQuantityGroupedByCondition(authenticatedUser)
                        .stream()
                        .map(item -> new CollectionAnalyticsItemResponse(
                                item.getCondition().name(),
                                item.getQuantity()
                        ))
                        .toList();

        List<CollectionAnalyticsItemResponse> rarities =
                cardRepository
                        .findQuantityGroupedByRarity(authenticatedUser)
                        .stream()
                        .map(item -> new CollectionAnalyticsItemResponse(
                                item.getRarity(),
                                item.getQuantity()
                        ))
                        .toList();

        return new CollectionAnalyticsResponse(
                collections,
                languages,
                conditions,
                rarities
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionGoalsResponse getCollectionGoals() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        long uniqueCards = cardRepository.count(
                CardSpecification.userEquals(authenticatedUser)
        );

        Long totalQuantity =
                cardRepository.sumTotalQuantity(authenticatedUser);

        long totalCards =
                totalQuantity != null ? totalQuantity : 0L;

        long differentLanguages =
                cardRepository.countDifferentLanguages(authenticatedUser);

        long differentCollections =
                cardRepository.countDifferentCollections(authenticatedUser);

        boolean hasFavorite =
                cardRepository.existsByUserAndFavoriteTrue(
                        authenticatedUser
                );

        long rareCards =
                cardRepository.countRareCards(authenticatedUser);

        List<CollectionGoalResponse> goals = List.of(
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

        long completedGoals = goals
                .stream()
                .filter(CollectionGoalResponse::completed)
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
                Math.min(currentValue, targetValue),
                targetValue,
                currentValue >= targetValue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionProgressResponse> getCollectionProgress() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        return cardRepository
                .findCollectionProgress(authenticatedUser)
                .stream()
                .map(item -> {
                    long ownedCards = item.getOwnedCards();
                    long totalCards = item.getCollectionTotal();

                    double completionPercentage =
                            totalCards > 0
                                    ? (ownedCards * 100.0) / totalCards
                                    : 0.0;

                    return new CollectionProgressResponse(
                            item.getCollectionId(),
                            item.getCollectionName(),
                            ownedCards,
                            totalCards,
                            Math.round(completionPercentage * 100.0) / 100.0
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public RefreshCardMetadataResponse refreshMetadata() {
        List<CardEntity> cards = cardRepository.findAll();

        long processed = 0;
        long updated = 0;

        for (CardEntity card : cards) {

            processed++;

            if (card.getCollectionId() != null &&
                    card.getCollectionTotal() != null) {
                continue;
            }

            PokemonCardApiSingleResponse response =
                    pokemonTcgClient.findById(card.getExternalId());

            PokemonCardApiData pokemonCard = response.data();

            if (pokemonCard.set() != null) {

                card.setCollectionId(
                        pokemonCard.set().id());

                card.setCollectionName(
                        pokemonCard.set().name());

                card.setCollectionTotal(
                        pokemonCard.set().total());

                updated++;
            }
        }

        cardRepository.saveAll(cards);

        return new RefreshCardMetadataResponse(
                processed,
                updated
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionDetailsResponse getCollectionDetails(String collectionId) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        List<CardEntity> cards =
                cardRepository.findByUserAndCollectionId(
                        authenticatedUser,
                        collectionId
                );

        if (cards.isEmpty()) {
            throw new CollectionNotFoundException(collectionId);
        }

        cards.sort(cardNumberComparator());

        CardEntity firstCard = cards.get(0);

        long ownedUniqueCards = cards.stream()
                .map(CardEntity::getExternalId)
                .distinct()
                .count();

        long totalCards = firstCard.getCollectionTotal() != null
                ? firstCard.getCollectionTotal()
                : 0;

        double completionPercentage = totalCards > 0
                ? (ownedUniqueCards * 100.0) / totalCards
                : 0.0;

        List<CollectionOwnedCardResponse> ownedCards = cards.stream()
                .map(card -> new CollectionOwnedCardResponse(
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
                ))
                .toList();

        return new CollectionDetailsResponse(
                firstCard.getCollectionId(),
                firstCard.getCollectionName(),
                ownedUniqueCards,
                totalCards,
                Math.round(completionPercentage * 100.0) / 100.0,
                ownedCards
        );
    }

    private Comparator<CardEntity> cardNumberComparator() {
        return Comparator.comparing(
                CardEntity::getCardNumber,
                this::compareCardNumbers
        );
    }

    private int compareCardNumbers(String first, String second) {
        if (first == null && second == null) {
            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        Matcher firstMatcher = CARD_NUMBER_PATTERN.matcher(first);
        Matcher secondMatcher = CARD_NUMBER_PATTERN.matcher(second);

        if (!firstMatcher.matches() || !secondMatcher.matches()) {
            return first.compareToIgnoreCase(second);
        }

        String firstPrefix = firstMatcher.group(1);
        String secondPrefix = secondMatcher.group(1);

        int prefixComparison =
                firstPrefix.compareToIgnoreCase(secondPrefix);

        if (prefixComparison != 0) {
            return prefixComparison;
        }

        long firstNumber = Long.parseLong(firstMatcher.group(2));
        long secondNumber = Long.parseLong(secondMatcher.group(2));

        int numberComparison =
                Long.compare(firstNumber, secondNumber);

        if (numberComparison != 0) {
            return numberComparison;
        }

        String firstSuffix = firstMatcher.group(3);
        String secondSuffix = secondMatcher.group(3);

        return firstSuffix.compareToIgnoreCase(secondSuffix);
    }
}