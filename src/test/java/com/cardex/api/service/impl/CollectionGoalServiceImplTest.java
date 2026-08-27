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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionGoalServiceImplTest {

    @Mock
    private CollectionGoalRepository collectionGoalRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PokemonCardCatalogRepository pokemonCardCatalogRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private CollectionGoalServiceImpl collectionGoalService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setName("Lucas");
        user.setEmail("lucas@example.com");
    }

    @Test
    void shouldCreateTotalCardsGoal() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Reach 100 cards",
                        CollectionGoalType.TOTAL_CARDS,
                        100,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        when(collectionGoalRepository.save(
                org.mockito.ArgumentMatchers.any(
                        CollectionGoalEntity.class
                )
        )).thenAnswer(invocation -> {
            CollectionGoalEntity goal =
                    invocation.getArgument(0);

            goal.setId(10L);

            return goal;
        });

        when(cardRepository.sumTotalQuantity(user))
                .thenReturn(37L);

        UserCollectionGoalResponse response =
                collectionGoalService.create(request);

        assertEquals(10L, response.id());
        assertEquals("Reach 100 cards", response.title());

        assertEquals(
                CollectionGoalType.TOTAL_CARDS,
                response.type()
        );

        assertEquals(100, response.targetValue());
        assertEquals(37, response.currentValue());
        assertEquals(100, response.goalValue());
        assertEquals(37.0, response.completionPercentage());
        assertFalse(response.completed());

        verify(collectionGoalRepository)
                .countByUser(user);

        verify(cardRepository)
                .sumTotalQuantity(user);
    }

    @Test
    void shouldCreateLanguageCardsGoal() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Collect 50 Japanese cards",
                        CollectionGoalType.LANGUAGE_CARDS,
                        50,
                        null,
                        CardLanguage.JAPANESE
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        when(collectionGoalRepository.save(
                org.mockito.ArgumentMatchers.any(
                        CollectionGoalEntity.class
                )
        )).thenAnswer(invocation -> {
            CollectionGoalEntity goal =
                    invocation.getArgument(0);

            goal.setId(11L);

            return goal;
        });

        when(
                cardRepository
                        .sumTotalQuantityByUserAndLanguage(
                                user,
                                CardLanguage.JAPANESE
                        )
        ).thenReturn(12L);

        UserCollectionGoalResponse response =
                collectionGoalService.create(request);

        assertEquals(
                CollectionGoalType.LANGUAGE_CARDS,
                response.type()
        );

        assertEquals(
                CardLanguage.JAPANESE,
                response.language()
        );

        assertEquals(12, response.currentValue());
        assertEquals(50, response.goalValue());
        assertEquals(24.0, response.completionPercentage());
        assertFalse(response.completed());
    }

    @Test
    void shouldCreateCollectionCompletionGoal() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Complete Base Set",
                        CollectionGoalType.COLLECTION_COMPLETION,
                        null,
                        " base1 ",
                        null
                );

        PokemonCardCatalogEntity firstCard =
                new PokemonCardCatalogEntity();

        firstCard.setExternalId("base1-1");
        firstCard.setCollectionId("base1");
        firstCard.setCollectionName("Base Set");

        PokemonCardCatalogEntity secondCard =
                new PokemonCardCatalogEntity();

        secondCard.setExternalId("base1-2");
        secondCard.setCollectionId("base1");
        secondCard.setCollectionName("Base Set");

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        when(
                pokemonCardCatalogRepository
                        .existsByCollectionId("base1")
        ).thenReturn(true);

        when(collectionGoalRepository.save(
                org.mockito.ArgumentMatchers.any(
                        CollectionGoalEntity.class
                )
        )).thenAnswer(invocation -> {
            CollectionGoalEntity goal =
                    invocation.getArgument(0);

            goal.setId(12L);

            return goal;
        });

        when(
                pokemonCardCatalogRepository
                        .findByCollectionId("base1")
        ).thenReturn(
                List.of(
                        firstCard,
                        secondCard
                )
        );

        when(
                cardRepository
                        .countDistinctCardsByUserAndCollectionId(
                                user,
                                "base1"
                        )
        ).thenReturn(1L);

        UserCollectionGoalResponse response =
                collectionGoalService.create(request);

        assertEquals(
                CollectionGoalType.COLLECTION_COMPLETION,
                response.type()
        );

        assertEquals("base1", response.collectionId());
        assertEquals("Base Set", response.collectionName());

        assertEquals(1, response.currentValue());
        assertEquals(2, response.goalValue());

        assertEquals(
                50.0,
                response.completionPercentage()
        );

        assertFalse(response.completed());
    }

    @Test
    void shouldLimitCompletionPercentageToOneHundred() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Reach 10 cards",
                        CollectionGoalType.TOTAL_CARDS,
                        10,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        when(collectionGoalRepository.save(
                org.mockito.ArgumentMatchers.any(
                        CollectionGoalEntity.class
                )
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(cardRepository.sumTotalQuantity(user))
                .thenReturn(25L);

        UserCollectionGoalResponse response =
                collectionGoalService.create(request);

        assertEquals(
                100.0,
                response.completionPercentage()
        );

        assertTrue(response.completed());

        assertEquals(
                25,
                response.currentValue()
        );

        assertEquals(
                10,
                response.goalValue()
        );
    }

    @Test
    void shouldRejectTotalCardsGoalWithZeroTarget() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Invalid goal",
                        CollectionGoalType.TOTAL_CARDS,
                        0,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.create(
                                        request
                                )
                );

        assertEquals(
                "Target value must be greater than zero.",
                exception.getMessage()
        );

        verify(collectionGoalRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectLanguageGoalWithoutLanguage() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Japanese cards",
                        CollectionGoalType.LANGUAGE_CARDS,
                        50,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.create(
                                        request
                                )
                );

        assertEquals(
                "Language is required for LANGUAGE_CARDS goals.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectCollectionGoalWithoutCollection() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Complete collection",
                        CollectionGoalType.COLLECTION_COMPLETION,
                        null,
                        " ",
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.create(
                                        request
                                )
                );

        assertEquals(
                "Collection is required for COLLECTION_COMPLETION goals.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectCollectionGoalWhenCollectionDoesNotExist() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Complete missing collection",
                        CollectionGoalType.COLLECTION_COMPLETION,
                        null,
                        "missing-set",
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(0L);

        when(
                pokemonCardCatalogRepository
                        .existsByCollectionId(
                                "missing-set"
                        )
        ).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.create(
                                        request
                                )
                );

        assertEquals(
                "Collection not found.",
                exception.getMessage()
        );

        verify(collectionGoalRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectGoalWhenUserReachedMaximumGoals() {
        CreateCollectionGoalRequest request =
                new CreateCollectionGoalRequest(
                        "Another goal",
                        CollectionGoalType.TOTAL_CARDS,
                        100,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(collectionGoalRepository.countByUser(user))
                .thenReturn(20L);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.create(
                                        request
                                )
                );

        assertEquals(
                "You can create up to 20 collection goals.",
                exception.getMessage()
        );

        verify(collectionGoalRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldReturnAuthenticatedUserGoals() {
        CollectionGoalEntity goal =
                CollectionGoalEntity.builder()
                        .user(user)
                        .title("Reach 100 cards")
                        .type(
                                CollectionGoalType.TOTAL_CARDS
                        )
                        .targetValue(100)
                        .build();

        goal.setId(10L);

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(
                collectionGoalRepository
                        .findAllByUserOrderByCreatedAtDesc(
                                user
                        )
        ).thenReturn(
                List.of(goal)
        );

        when(cardRepository.sumTotalQuantity(user))
                .thenReturn(40L);

        List<UserCollectionGoalResponse> result =
                collectionGoalService.findAll();

        assertEquals(1, result.size());

        assertEquals(
                "Reach 100 cards",
                result.get(0).title()
        );

        verify(collectionGoalRepository)
                .findAllByUserOrderByCreatedAtDesc(
                        user
                );
    }

    @Test
    void shouldUpdateGoal() {
        CollectionGoalEntity existingGoal =
                CollectionGoalEntity.builder()
                        .user(user)
                        .title("Old goal")
                        .type(
                                CollectionGoalType.TOTAL_CARDS
                        )
                        .targetValue(10)
                        .build();

        existingGoal.setId(10L);

        UpdateCollectionGoalRequest request =
                new UpdateCollectionGoalRequest(
                        "Collect Japanese cards",
                        CollectionGoalType.LANGUAGE_CARDS,
                        30,
                        null,
                        CardLanguage.JAPANESE
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(
                collectionGoalRepository
                        .findByIdAndUser(
                                10L,
                                user
                        )
        ).thenReturn(
                Optional.of(existingGoal)
        );

        when(
                collectionGoalRepository.save(
                        existingGoal
                )
        ).thenReturn(
                existingGoal
        );

        when(
                cardRepository
                        .sumTotalQuantityByUserAndLanguage(
                                user,
                                CardLanguage.JAPANESE
                        )
        ).thenReturn(15L);

        UserCollectionGoalResponse result =
                collectionGoalService.update(
                        10L,
                        request
                );

        assertEquals(
                "Collect Japanese cards",
                existingGoal.getTitle()
        );

        assertEquals(
                CollectionGoalType.LANGUAGE_CARDS,
                existingGoal.getType()
        );

        assertEquals(
                CardLanguage.JAPANESE,
                existingGoal.getLanguage()
        );

        assertEquals(
                30,
                existingGoal.getTargetValue()
        );

        assertEquals(
                15,
                result.currentValue()
        );

        assertEquals(
                50.0,
                result.completionPercentage()
        );
    }

    @Test
    void shouldRejectUpdateWhenGoalDoesNotBelongToUser() {
        UpdateCollectionGoalRequest request =
                new UpdateCollectionGoalRequest(
                        "Updated goal",
                        CollectionGoalType.TOTAL_CARDS,
                        100,
                        null,
                        null
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(
                collectionGoalRepository
                        .findByIdAndUser(
                                99L,
                                user
                        )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.update(
                                        99L,
                                        request
                                )
                );

        assertEquals(
                "Collection goal not found.",
                exception.getMessage()
        );

        verify(collectionGoalRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldDeleteGoal() {
        CollectionGoalEntity goal =
                CollectionGoalEntity.builder()
                        .user(user)
                        .title("Goal")
                        .type(
                                CollectionGoalType.TOTAL_CARDS
                        )
                        .targetValue(100)
                        .build();

        goal.setId(10L);

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(
                collectionGoalRepository
                        .findByIdAndUser(
                                10L,
                                user
                        )
        ).thenReturn(
                Optional.of(goal)
        );

        collectionGoalService.delete(10L);

        verify(collectionGoalRepository)
                .delete(goal);
    }

    @Test
    void shouldRejectDeleteWhenGoalDoesNotBelongToUser() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(
                collectionGoalRepository
                        .findByIdAndUser(
                                99L,
                                user
                        )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                collectionGoalService.delete(
                                        99L
                                )
                );

        assertEquals(
                "Collection goal not found.",
                exception.getMessage()
        );

        verify(collectionGoalRepository, never())
                .delete(
                        org.mockito.ArgumentMatchers.any()
                );
    }
}