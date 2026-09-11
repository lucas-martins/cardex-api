package com.cardex.api.service.impl;

import com.cardex.api.dto.wishlist.UpdateWishlistPriorityRequest;
import com.cardex.api.dto.wishlist.WishlistCardRequest;
import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.entity.WishlistCardEntity;
import com.cardex.api.enumeration.WishlistPriority;
import com.cardex.api.exception.WishlistCardAlreadyExistsException;
import com.cardex.api.exception.WishlistCardNotFoundException;
import com.cardex.api.mapper.WishlistCardMapper;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.ExchangeRateService;
import com.cardex.api.service.PokemonCardCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistCardServiceImplTest {

    @Mock
    private WishlistCardRepository repository;

    @Mock
    private WishlistCardMapper mapper;

    @Mock
    private PokemonCardCatalogService pokemonCardCatalogService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private WishlistCardServiceImpl wishlistCardService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("test@example.com");
    }

    @Test
    void shouldCreateWishlistCardWithPriority() {
        WishlistCardRequest request =
                new WishlistCardRequest(
                        "sm1-1",
                        WishlistPriority.HIGH
                );

        PokemonCardCatalogEntity catalogCard =
                createCatalogCard();

        WishlistCardEntity savedEntity =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .cardNumber("1")
                        .collectionId("sm1")
                        .collectionName("Sun & Moon")
                        .series("Sun & Moon")
                        .rarity("Common")
                        .imageUrl("image-url")
                        .priority(WishlistPriority.HIGH)
                        .build();

        WishlistCardResponse response =
                createResponse(
                        WishlistPriority.HIGH
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.existsByUserAndExternalId(
                user,
                "sm1-1"
        )).thenReturn(false);

        when(pokemonCardCatalogService.findByExternalId(
                "sm1-1"
        )).thenReturn(catalogCard);

        when(repository.save(any(WishlistCardEntity.class)))
                .thenReturn(savedEntity);

        when(mapper.toResponse(savedEntity))
                .thenReturn(response);

        WishlistCardResponse result =
                wishlistCardService.create(request);

        assertEquals(
                WishlistPriority.HIGH,
                result.priority()
        );

        verify(repository)
                .existsByUserAndExternalId(
                        user,
                        "sm1-1"
                );

        verify(pokemonCardCatalogService)
                .findByExternalId("sm1-1");

        verify(repository)
                .save(any(WishlistCardEntity.class));
    }

    @Test
    void shouldUseMediumPriorityWhenPriorityIsNotProvided() {
        WishlistCardRequest request =
                new WishlistCardRequest(
                        "sm1-1",
                        null
                );

        PokemonCardCatalogEntity catalogCard =
                createCatalogCard();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.existsByUserAndExternalId(
                user,
                "sm1-1"
        )).thenReturn(false);

        when(pokemonCardCatalogService.findByExternalId(
                "sm1-1"
        )).thenReturn(catalogCard);

        when(repository.save(any(WishlistCardEntity.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(mapper.toResponse(any(WishlistCardEntity.class)))
                .thenAnswer(invocation -> {
                    WishlistCardEntity entity =
                            invocation.getArgument(0);

                    return createResponse(
                            entity.getPriority()
                    );
                });

        WishlistCardResponse result =
                wishlistCardService.create(request);

        assertEquals(
                WishlistPriority.MEDIUM,
                result.priority()
        );
    }

    @Test
    void shouldThrowExceptionWhenWishlistCardAlreadyExists() {
        WishlistCardRequest request =
                new WishlistCardRequest(
                        "sm1-1",
                        WishlistPriority.MEDIUM
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.existsByUserAndExternalId(
                user,
                "sm1-1"
        )).thenReturn(true);

        assertThrows(
                WishlistCardAlreadyExistsException.class,
                () -> wishlistCardService.create(request)
        );

        verifyNoInteractions(
                pokemonCardCatalogService
        );

        verify(repository, never())
                .save(any(WishlistCardEntity.class));
    }

    @Test
    void shouldReturnWishlistCards() {
        WishlistCardEntity entity =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .priority(WishlistPriority.MEDIUM)
                        .build();

        WishlistCardResponse response =
                createResponse(
                        WishlistPriority.MEDIUM
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.findAllByUserOrderByCreatedAtDesc(
                user
        )).thenReturn(List.of(entity));

        when(mapper.toResponse(entity))
                .thenReturn(response);

        List<WishlistCardResponse> result =
                wishlistCardService.findAll();

        assertEquals(1, result.size());

        assertEquals(
                "sm1-1",
                result.get(0).externalId()
        );

        assertEquals(
                WishlistPriority.MEDIUM,
                result.get(0).priority()
        );
    }

    @Test
    void shouldUpdateWishlistCardPriority() {
        WishlistCardEntity entity =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .priority(WishlistPriority.MEDIUM)
                        .build();

        UpdateWishlistPriorityRequest request =
                new UpdateWishlistPriorityRequest(
                        WishlistPriority.HIGH
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(entity));

        when(repository.save(entity))
                .thenReturn(entity);

        when(mapper.toResponse(entity))
                .thenAnswer(invocation ->
                        createResponse(
                                entity.getPriority()
                        )
                );

        WishlistCardResponse result =
                wishlistCardService.updatePriority(
                        1L,
                        request
                );

        assertEquals(
                WishlistPriority.HIGH,
                entity.getPriority()
        );

        assertEquals(
                WishlistPriority.HIGH,
                result.priority()
        );

        verify(repository)
                .save(entity);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUnknownWishlistCard() {
        UpdateWishlistPriorityRequest request =
                new UpdateWishlistPriorityRequest(
                        WishlistPriority.HIGH
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.findByIdAndUser(
                99L,
                user
        )).thenReturn(Optional.empty());

        assertThrows(
                WishlistCardNotFoundException.class,
                () -> wishlistCardService.updatePriority(
                        99L,
                        request
                )
        );

        verify(repository, never())
                .save(any(WishlistCardEntity.class));
    }

    @Test
    void shouldDeleteWishlistCard() {
        WishlistCardEntity entity =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .priority(WishlistPriority.MEDIUM)
                        .build();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(entity));

        wishlistCardService.delete(1L);

        verify(repository)
                .delete(entity);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUnknownWishlistCard() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(repository.findByIdAndUser(
                99L,
                user
        )).thenReturn(Optional.empty());

        assertThrows(
                WishlistCardNotFoundException.class,
                () -> wishlistCardService.delete(
                        99L
                )
        );

        verify(repository, never())
                .delete(any(WishlistCardEntity.class));
    }

    private PokemonCardCatalogEntity createCatalogCard() {
        PokemonCardCatalogEntity card =
                new PokemonCardCatalogEntity();

        card.setExternalId("sm1-1");
        card.setName("Caterpie");
        card.setCardNumber("1");
        card.setCollectionId("sm1");
        card.setCollectionName("Sun & Moon");
        card.setCollectionSeries("Sun & Moon");
        card.setRarity("Common");
        card.setImageUrl("image-url");

        return card;
    }

    private WishlistCardResponse createResponse(
            WishlistPriority priority
    ) {
        return new WishlistCardResponse(
                1L,
                "sm1-1",
                "Caterpie",
                "1",
                "sm1",
                "Sun & Moon",
                "Sun & Moon",
                "Common",
                "image-url",
                priority,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }
}