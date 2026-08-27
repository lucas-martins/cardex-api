package com.cardex.api.service.impl;

import com.cardex.api.dto.request.CreateCollectionGoalRequest;
import com.cardex.api.dto.request.UpdateCollectionGoalRequest;
import com.cardex.api.dto.response.UserCollectionGoalResponse;
import com.cardex.api.entity.CollectionGoalEntity;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.enumeration.CollectionGoalType;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.CollectionGoalRepository;
import com.cardex.api.repository.PokemonCardCatalogRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.CollectionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionGoalServiceImpl
        implements CollectionGoalService {

    private static final int MAX_GOALS_PER_USER = 20;

    private final CollectionGoalRepository collectionGoalRepository;
    private final CardRepository cardRepository;
    private final PokemonCardCatalogRepository pokemonCardCatalogRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional
    public UserCollectionGoalResponse create(
            CreateCollectionGoalRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        validateGoalLimit(authenticatedUser);

        GoalConfiguration configuration =
                validateAndNormalize(
                        request.type(),
                        request.targetValue(),
                        request.collectionId(),
                        request.language()
                );

        CollectionGoalEntity goal =
                CollectionGoalEntity.builder()
                        .user(authenticatedUser)
                        .title(request.title().trim())
                        .type(request.type())
                        .targetValue(
                                configuration.targetValue()
                        )
                        .collectionId(
                                configuration.collectionId()
                        )
                        .language(
                                configuration.language()
                        )
                        .build();

        CollectionGoalEntity savedGoal =
                collectionGoalRepository.save(goal);

        return toResponse(savedGoal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCollectionGoalResponse> findAll() {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        return collectionGoalRepository
                .findAllByUserOrderByCreatedAtDesc(
                        authenticatedUser
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserCollectionGoalResponse update(
            Long id,
            UpdateCollectionGoalRequest request
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CollectionGoalEntity goal =
                collectionGoalRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Collection goal not found."
                                        )
                        );

        GoalConfiguration configuration =
                validateAndNormalize(
                        request.type(),
                        request.targetValue(),
                        request.collectionId(),
                        request.language()
                );

        goal.setTitle(
                request.title().trim()
        );

        goal.setType(
                request.type()
        );

        goal.setTargetValue(
                configuration.targetValue()
        );

        goal.setCollectionId(
                configuration.collectionId()
        );

        goal.setLanguage(
                configuration.language()
        );

        CollectionGoalEntity savedGoal =
                collectionGoalRepository.save(goal);

        return toResponse(savedGoal);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        CollectionGoalEntity goal =
                collectionGoalRepository
                        .findByIdAndUser(
                                id,
                                authenticatedUser
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Collection goal not found."
                                        )
                        );

        collectionGoalRepository.delete(goal);
    }

    private void validateGoalLimit(
            UserEntity authenticatedUser
    ) {
        long currentGoals =
                collectionGoalRepository.countByUser(
                        authenticatedUser
                );

        if (currentGoals >= MAX_GOALS_PER_USER) {
            throw new IllegalArgumentException(
                    "You can create up to "
                            + MAX_GOALS_PER_USER
                            + " collection goals."
            );
        }
    }

    private GoalConfiguration validateAndNormalize(
            CollectionGoalType type,
            Integer targetValue,
            String collectionId,
            CardLanguage language
    ) {
        return switch (type) {
            case TOTAL_CARDS ->
                    validateTotalCardsGoal(
                            targetValue
                    );

            case COLLECTION_COMPLETION ->
                    validateCollectionCompletionGoal(
                            collectionId
                    );

            case LANGUAGE_CARDS ->
                    validateLanguageCardsGoal(
                            targetValue,
                            language
                    );
        };
    }

    private GoalConfiguration validateTotalCardsGoal(
            Integer targetValue
    ) {
        validatePositiveTargetValue(
                targetValue
        );

        return new GoalConfiguration(
                targetValue,
                null,
                null
        );
    }

    private GoalConfiguration
    validateCollectionCompletionGoal(
            String collectionId
    ) {
        String normalizedCollectionId =
                normalizeRequiredCollectionId(
                        collectionId
                );

        if (!pokemonCardCatalogRepository
                .existsByCollectionId(
                        normalizedCollectionId
                )) {
            throw new IllegalArgumentException(
                    "Collection not found."
            );
        }

        return new GoalConfiguration(
                null,
                normalizedCollectionId,
                null
        );
    }

    private GoalConfiguration validateLanguageCardsGoal(
            Integer targetValue,
            CardLanguage language
    ) {
        validatePositiveTargetValue(
                targetValue
        );

        if (language == null) {
            throw new IllegalArgumentException(
                    "Language is required for LANGUAGE_CARDS goals."
            );
        }

        return new GoalConfiguration(
                targetValue,
                null,
                language
        );
    }

    private void validatePositiveTargetValue(
            Integer targetValue
    ) {
        if (targetValue == null
                || targetValue <= 0) {
            throw new IllegalArgumentException(
                    "Target value must be greater than zero."
            );
        }
    }

    private String normalizeRequiredCollectionId(
            String collectionId
    ) {
        if (collectionId == null
                || collectionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Collection is required for COLLECTION_COMPLETION goals."
            );
        }

        return collectionId.trim();
    }

    private UserCollectionGoalResponse toResponse(
            CollectionGoalEntity goal
    ) {
        GoalProgress progress =
                calculateProgress(goal);

        return new UserCollectionGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getType(),
                goal.getTargetValue(),
                goal.getCollectionId(),
                progress.collectionName(),
                goal.getLanguage(),
                progress.currentValue(),
                progress.goalValue(),
                progress.completionPercentage(),
                progress.completed(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private GoalProgress calculateProgress(
            CollectionGoalEntity goal
    ) {
        return switch (goal.getType()) {
            case TOTAL_CARDS ->
                    calculateTotalCardsProgress(
                            goal
                    );

            case COLLECTION_COMPLETION ->
                    calculateCollectionCompletionProgress(
                            goal
                    );

            case LANGUAGE_CARDS ->
                    calculateLanguageCardsProgress(
                            goal
                    );
        };
    }

    private GoalProgress calculateTotalCardsProgress(
            CollectionGoalEntity goal
    ) {
        Long totalQuantity =
                cardRepository.sumTotalQuantity(
                        goal.getUser()
                );

        int currentValue =
                totalQuantity != null
                        ? Math.toIntExact(
                        totalQuantity
                )
                        : 0;

        int goalValue =
                goal.getTargetValue();

        return buildProgress(
                currentValue,
                goalValue,
                null
        );
    }

    private GoalProgress calculateLanguageCardsProgress(
            CollectionGoalEntity goal
    ) {
        Long totalQuantity =
                cardRepository
                        .sumTotalQuantityByUserAndLanguage(
                                goal.getUser(),
                                goal.getLanguage()
                        );

        int currentValue =
                totalQuantity != null
                        ? Math.toIntExact(
                        totalQuantity
                )
                        : 0;

        int goalValue =
                goal.getTargetValue();

        return buildProgress(
                currentValue,
                goalValue,
                null
        );
    }

    private GoalProgress
    calculateCollectionCompletionProgress(
            CollectionGoalEntity goal
    ) {
        List<PokemonCardCatalogEntity> catalogCards =
                pokemonCardCatalogRepository
                        .findByCollectionId(
                                goal.getCollectionId()
                        );

        int goalValue =
                catalogCards.size();

        int currentValue =
                Math.toIntExact(
                        cardRepository
                                .countDistinctCardsByUserAndCollectionId(
                                        goal.getUser(),
                                        goal.getCollectionId()
                                )
                );

        String collectionName =
                catalogCards
                        .stream()
                        .findFirst()
                        .map(
                                PokemonCardCatalogEntity::
                                        getCollectionName
                        )
                        .orElse(null);

        return buildProgress(
                currentValue,
                goalValue,
                collectionName
        );
    }

    private GoalProgress buildProgress(
            int currentValue,
            int goalValue,
            String collectionName
    ) {
        double completionPercentage;

        if (goalValue <= 0) {
            completionPercentage = 0.0;
        } else {
            completionPercentage =
                    Math.min(
                            100.0,
                            (currentValue * 100.0)
                                    / goalValue
                    );
        }

        boolean completed =
                goalValue > 0
                        && currentValue >= goalValue;

        return new GoalProgress(
                currentValue,
                goalValue,
                roundPercentage(
                        completionPercentage
                ),
                completed,
                collectionName
        );
    }

    private double roundPercentage(
            double value
    ) {
        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private record GoalConfiguration(
            Integer targetValue,
            String collectionId,
            CardLanguage language
    ) {
    }

    private record GoalProgress(
            int currentValue,
            int goalValue,
            double completionPercentage,
            boolean completed,
            String collectionName
    ) {
    }
}