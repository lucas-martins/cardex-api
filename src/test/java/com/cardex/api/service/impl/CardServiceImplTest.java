package com.cardex.api.service.impl;

import com.cardex.api.component.CardHistoryRecorder;
import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.CollectionChecklistResponse;
import com.cardex.api.entity.*;
import com.cardex.api.enumeration.*;
import com.cardex.api.exception.CardNotFoundException;
import com.cardex.api.exception.CollectionNotFoundException;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.*;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.WishlistCardRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.ExchangeRateService;
import com.cardex.api.service.PokemonCardCatalogService;
import com.cardex.api.service.PokemonSetCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardHistoryRecorder cardHistoryRecorder;

    @Mock
    private PokemonCardCatalogService pokemonCardCatalogService;

    @Mock
    private WishlistCardRepository wishlistCardRepository;

    @Mock
    private PokemonSetCatalogService pokemonSetCatalogService;

    @Mock
    private ExchangeRateService exchangeRateService;

    private CardEntity cardEntity;
    private CardResponse cardResponse;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("test@example.com");

        cardEntity = new CardEntity();
        cardEntity.setName("Decidueye-GX");
        cardEntity.setUser(user);

        cardResponse = CardResponse.builder()
                .id(1L)
                .name("Decidueye-GX")
                .build();
    }

    @Test
    void shouldFindCardById() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(cardEntity));

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(cardResponse);

        CardResponse result = cardService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Decidueye-GX", result.getName());

        verify(authenticatedUserService).getAuthenticatedUser();
        verify(cardRepository).findByIdAndUser(1L, user);
        verify(cardMapper).toResponse(cardEntity);
    }

    @Test
    void shouldThrowExceptionWhenCardDoesNotExist() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> cardService.findById(999L)
        );

        assertEquals(
                "Card not found for ID: 999",
                exception.getMessage()
        );

        verify(authenticatedUserService).getAuthenticatedUser();
        verify(cardRepository).findByIdAndUser(999L, user);
    }

    @Test
    void shouldUpdateCard() {
        UpdateCardRequest request = new UpdateCardRequest();
        request.setQuantity(3);
        request.setLanguage(CardLanguage.PORTUGUESE);
        request.setCondition(CardCondition.MINT);
        request.setNotes("Updated card");

        cardEntity.setQuantity(1);
        cardEntity.setLanguage(CardLanguage.ENGLISH);
        cardEntity.setCondition(CardCondition.NEAR_MINT);
        cardEntity.setNotes(null);

        CardResponse updatedResponse = CardResponse.builder()
                .id(1L)
                .name("Decidueye-GX")
                .quantity(3)
                .language(CardLanguage.PORTUGUESE)
                .condition(CardCondition.MINT)
                .notes("Updated card")
                .build();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(cardEntity));

        doAnswer(invocation -> {
            UpdateCardRequest updateRequest = invocation.getArgument(0);
            CardEntity entity = invocation.getArgument(1);

            entity.setQuantity(updateRequest.getQuantity());
            entity.setLanguage(updateRequest.getLanguage());
            entity.setCondition(updateRequest.getCondition());
            entity.setNotes(updateRequest.getNotes());

            return null;
        }).when(cardMapper).updateEntity(request, cardEntity);

        when(cardRepository.save(cardEntity))
                .thenReturn(cardEntity);

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(updatedResponse);

        CardResponse result = cardService.update(1L, request);

        assertEquals(3, cardEntity.getQuantity());
        assertEquals(CardLanguage.PORTUGUESE, cardEntity.getLanguage());
        assertEquals(CardCondition.MINT, cardEntity.getCondition());
        assertEquals("Updated card", cardEntity.getNotes());
        assertEquals(3, result.getQuantity());

        verify(authenticatedUserService).getAuthenticatedUser();
        verify(cardRepository).findByIdAndUser(1L, user);
        verify(cardMapper).updateEntity(request, cardEntity);
        verify(cardRepository).save(cardEntity);
        verify(cardMapper).toResponse(cardEntity);
        verify(cardHistoryRecorder).record(
                eq(cardEntity),
                eq(CardHistoryAction.UPDATED),
                contains("Quantity changed from 1 to 3.")
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentCard() {
        UpdateCardRequest request = new UpdateCardRequest();
        request.setQuantity(3);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);
        request.setNotes("Updated card");

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByIdAndUser(999L, user))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> cardService.update(999L, request)
        );

        assertEquals(
                "Card not found for ID: 999",
                exception.getMessage()
        );

        verify(authenticatedUserService).getAuthenticatedUser();
        verify(cardRepository).findByIdAndUser(999L, user);
        verify(cardRepository, never()).save(any(CardEntity.class));
    }

    @Test
    void shouldIncreaseQuantityWhenCardAlreadyExists() {
        CreateCardRequest request = new CreateCardRequest();
        request.setExternalId("sm1-12");
        request.setQuantity(2);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);
        request.setNotes("Repeated card");

        cardEntity.setQuantity(3);
        cardEntity.setExternalId("sm1-12");
        cardEntity.setLanguage(CardLanguage.ENGLISH);
        cardEntity.setCondition(CardCondition.NEAR_MINT);

        CardResponse updatedResponse = CardResponse.builder()
                .id(1L)
                .externalId("sm1-12")
                .quantity(5)
                .language(CardLanguage.ENGLISH)
                .condition(CardCondition.NEAR_MINT)
                .build();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndExternalIdAndLanguageAndCondition(
                user,
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(Optional.of(cardEntity));

        when(cardRepository.save(cardEntity))
                .thenReturn(cardEntity);

        WishlistCardEntity wishlistCard =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-12")
                        .name("Decidueye-GX")
                        .priority(WishlistPriority.MEDIUM)
                        .build();

        when(wishlistCardRepository.findByUserAndExternalId(
                user,
                "sm1-12"
        )).thenReturn(Optional.of(wishlistCard));

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(updatedResponse);

        CardResponse result = cardService.create(request);

        assertEquals(5, cardEntity.getQuantity());
        assertEquals(5, result.getQuantity());

        verify(cardRepository)
                .findByUserAndExternalIdAndLanguageAndCondition(
                        user,
                        "sm1-12",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                );
        verify(cardRepository).save(cardEntity);
        verify(cardMapper).toResponse(cardEntity);
        verify(authenticatedUserService).getAuthenticatedUser();
        verifyNoInteractions(pokemonTcgClient);
        verify(cardHistoryRecorder).record(
                cardEntity,
                CardHistoryAction.UPDATED,
                "Quantity changed from 3 to 5."
        );

        verify(wishlistCardRepository)
                .findByUserAndExternalId(
                        user,
                        "sm1-12"
                );

        verify(wishlistCardRepository)
                .delete(wishlistCard);
    }

    @Test
    void shouldCreateNewCardUsingPokemonTcgApiData() {
        CreateCardRequest request = new CreateCardRequest();
        request.setExternalId("sm1-12");
        request.setQuantity(1);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);
        request.setNotes("First card");

        PokemonCardCatalogEntity catalogCard =
                new PokemonCardCatalogEntity();

        catalogCard.setExternalId("sm1-12");
        catalogCard.setName("Decidueye-GX");
        catalogCard.setCollectionId("sm1");
        catalogCard.setCollectionName("Sun & Moon");
        catalogCard.setCollectionTotal(163);
        catalogCard.setCardNumber("12");
        catalogCard.setRarity("Rare Holo GX");
        catalogCard.setImageUrl(
                "https://images.pokemontcg.io/sm1/12_hires.png"
        );

        CardEntity newCardEntity = new CardEntity();
        newCardEntity.setExternalId("sm1-12");
        newCardEntity.setQuantity(1);
        newCardEntity.setLanguage(CardLanguage.ENGLISH);
        newCardEntity.setCondition(CardCondition.NEAR_MINT);
        newCardEntity.setNotes("First card");

        CardResponse savedResponse = CardResponse.builder()
                .id(1L)
                .externalId("sm1-12")
                .name("Decidueye-GX")
                .collectionName("Sun & Moon")
                .cardNumber("12")
                .rarity("Rare Holo GX")
                .quantity(1)
                .language(CardLanguage.ENGLISH)
                .condition(CardCondition.NEAR_MINT)
                .imageUrl(
                        "https://images.pokemontcg.io/sm1/12_hires.png"
                )
                .notes("First card")
                .build();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository
                .findByUserAndExternalIdAndLanguageAndCondition(
                        user,
                        "sm1-12",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                )
        ).thenReturn(Optional.empty());

        when(pokemonCardCatalogService.findByExternalId(
                "sm1-12"
        )).thenReturn(catalogCard);

        when(cardMapper.toEntity(request))
                .thenReturn(newCardEntity);

        when(cardRepository.save(newCardEntity))
                .thenReturn(newCardEntity);

        when(cardMapper.toResponse(newCardEntity))
                .thenReturn(savedResponse);

        CardResponse result =
                cardService.create(request);

        assertEquals(user, newCardEntity.getUser());
        assertEquals(
                "Decidueye-GX",
                newCardEntity.getName()
        );
        assertEquals(
                "Sun & Moon",
                newCardEntity.getCollectionName()
        );
        assertEquals(
                "12",
                newCardEntity.getCardNumber()
        );
        assertEquals(
                "Rare Holo GX",
                newCardEntity.getRarity()
        );
        assertEquals(
                "https://images.pokemontcg.io/sm1/12_hires.png",
                newCardEntity.getImageUrl()
        );

        assertEquals(1L, result.getId());
        assertEquals(
                "Decidueye-GX",
                result.getName()
        );

        verify(pokemonCardCatalogService)
                .findByExternalId("sm1-12");

        verify(cardMapper)
                .toEntity(request);

        verify(cardRepository)
                .save(newCardEntity);

        verify(cardMapper)
                .toResponse(newCardEntity);

        verify(authenticatedUserService)
                .getAuthenticatedUser();

        verify(cardHistoryRecorder)
                .record(
                        newCardEntity,
                        CardHistoryAction.ADDED,
                        "Card added to collection."
                );
    }

    @Test
    void shouldThrowExceptionWhenPokemonCardIsNotFound() {
        CreateCardRequest request =
                new CreateCardRequest();

        request.setExternalId("invalid-id");
        request.setQuantity(1);
        request.setLanguage(
                CardLanguage.ENGLISH
        );
        request.setCondition(
                CardCondition.NEAR_MINT
        );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository
                .findByUserAndExternalIdAndLanguageAndCondition(
                        user,
                        "invalid-id",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                )
        ).thenReturn(Optional.empty());

        when(pokemonCardCatalogService.findByExternalId(
                "invalid-id"
        )).thenThrow(
                new PokemonCardNotFoundException(
                        "invalid-id"
                )
        );

        PokemonCardNotFoundException exception =
                assertThrows(
                        PokemonCardNotFoundException.class,
                        () -> cardService.create(request)
                );

        assertEquals(
                "Pokemon card not found for external ID: invalid-id",
                exception.getMessage()
        );

        verify(cardRepository)
                .findByUserAndExternalIdAndLanguageAndCondition(
                        user,
                        "invalid-id",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                );

        verify(pokemonCardCatalogService)
                .findByExternalId("invalid-id");

        verify(
                cardRepository,
                never()
        ).save(any(CardEntity.class));

        verify(authenticatedUserService)
                .getAuthenticatedUser();
    }

    @Test
    void shouldReturnCollectionChecklist() {
        CardEntity ownedCard = new CardEntity();
        ownedCard.setId(10L);
        ownedCard.setExternalId("sm1-2");
        ownedCard.setCollectionId("sm1");
        ownedCard.setCollectionName("Sun & Moon");
        ownedCard.setUser(user);

        PokemonCardCatalogEntity caterpie =
                new PokemonCardCatalogEntity();

        caterpie.setExternalId("sm1-1");
        caterpie.setName("Caterpie");
        caterpie.setCardNumber("1");
        caterpie.setRarity("Common");
        caterpie.setCollectionId("sm1");
        caterpie.setCollectionName("Sun & Moon");
        caterpie.setImageUrl("caterpie-large");

        PokemonCardCatalogEntity metapod =
                new PokemonCardCatalogEntity();

        metapod.setExternalId("sm1-2");
        metapod.setName("Metapod");
        metapod.setCardNumber("2");
        metapod.setRarity("Uncommon");
        metapod.setCollectionId("sm1");
        metapod.setCollectionName("Sun & Moon");
        metapod.setImageUrl("metapod-large");

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of(ownedCard));

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(caterpie, metapod)
        );

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals("sm1", result.collectionId());
        assertEquals("Sun & Moon", result.collectionName());
        assertEquals(1L, result.ownedUniqueCards());
        assertEquals(2L, result.totalCards());

        assertFalse(result.cards().get(0).owned());
        assertNull(result.cards().get(0).cardId());

        assertTrue(result.cards().get(1).owned());
        assertEquals(
                10L,
                result.cards().get(1).cardId()
        );

        verify(authenticatedUserService)
                .getAuthenticatedUser();

        verify(cardRepository)
                .findByUserAndCollectionId(
                        user,
                        "sm1"
                );

        verify(pokemonCardCatalogService)
                .findByCollectionId("sm1");
    }

    @Test
    void shouldCalculateCollectionChecklistPercentage() {
        CardEntity ownedCard = new CardEntity();
        ownedCard.setId(10L);
        ownedCard.setExternalId("sm1-1");
        ownedCard.setCollectionId("sm1");
        ownedCard.setCollectionName("Sun & Moon");
        ownedCard.setUser(user);

        PokemonCardCatalogEntity firstCard =
                createCatalogCard(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common"
                );

        PokemonCardCatalogEntity secondCard =
                createCatalogCard(
                        "sm1-2",
                        "Metapod",
                        "2",
                        "Uncommon"
                );

        PokemonCardCatalogEntity thirdCard =
                createCatalogCard(
                        "sm1-3",
                        "Butterfree",
                        "3",
                        "Rare"
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of(ownedCard));

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(
                        firstCard,
                        secondCard,
                        thirdCard
                )
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals(
                33.33,
                result.completionPercentage()
        );
    }

    private PokemonSetCatalogEntity createPokemonSetCatalog() {
        PokemonSetCatalogEntity pokemonSet =
                new PokemonSetCatalogEntity();

        pokemonSet.setCollectionId("sm1");
        pokemonSet.setName("Sun & Moon");
        pokemonSet.setPrintedTotal(149);
        pokemonSet.setTotal(163);

        return pokemonSet;
    }

    private PokemonCardCatalogEntity createCatalogCard(
            String externalId,
            String name,
            String cardNumber,
            String rarity
    ) {
        PokemonCardCatalogEntity card =
                new PokemonCardCatalogEntity();

        card.setExternalId(externalId);
        card.setName(name);
        card.setCardNumber(cardNumber);
        card.setRarity(rarity);
        card.setCollectionId("sm1");
        card.setCollectionName("Sun & Moon");

        return card;
    }

    @Test
    void shouldThrowExceptionWhenCollectionChecklistDoesNotExist() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "invalid"
        )).thenReturn(List.of());

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "invalid"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "invalid"
        )).thenReturn(List.of());

        CollectionNotFoundException exception =
                assertThrows(
                        CollectionNotFoundException.class,
                        () -> cardService
                                .getCollectionChecklist(
                                        "invalid"
                                )
                );

        assertEquals(
                "Collection not found for ID: invalid",
                exception.getMessage()
        );

        verify(pokemonCardCatalogService)
                .findByCollectionId(
                        "invalid"
                );
    }

    @Test
    void shouldReturnWishlistInformationInCollectionChecklist() {
        CardEntity ownedCard = new CardEntity();
        ownedCard.setId(10L);
        ownedCard.setExternalId("sm1-2");
        ownedCard.setCollectionId("sm1");
        ownedCard.setCollectionName("Sun & Moon");
        ownedCard.setUser(user);

        PokemonCardCatalogEntity caterpie =
                createCatalogCard(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common"
                );

        PokemonCardCatalogEntity metapod =
                createCatalogCard(
                        "sm1-2",
                        "Metapod",
                        "2",
                        "Uncommon"
                );

        WishlistCardEntity wishlistCard =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .collectionId("sm1")
                        .collectionName("Sun & Moon")
                        .priority(WishlistPriority.HIGH)
                        .build();

        wishlistCard.setId(20L);

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of(ownedCard));

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of(wishlistCard));

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(
                        caterpie,
                        metapod
                )
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        var caterpieResult =
                result.cards().get(0);

        assertFalse(caterpieResult.owned());
        assertNull(caterpieResult.cardId());

        assertTrue(caterpieResult.inWishlist());
        assertEquals(
                20L,
                caterpieResult.wishlistId()
        );
        assertEquals(
                WishlistPriority.HIGH,
                caterpieResult.wishlistPriority()
        );

        var metapodResult =
                result.cards().get(1);

        assertTrue(metapodResult.owned());
        assertFalse(metapodResult.inWishlist());
        assertNull(metapodResult.wishlistId());
        assertNull(
                metapodResult.wishlistPriority()
        );
    }

    @Test
    void shouldRemoveCardFromWishlistWhenAddingToCollection() {
        CreateCardRequest request =
                new CreateCardRequest();

        request.setExternalId("sm1-1");
        request.setQuantity(1);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);

        PokemonCardCatalogEntity catalogCard =
                new PokemonCardCatalogEntity();

        catalogCard.setExternalId("sm1-1");
        catalogCard.setName("Caterpie");
        catalogCard.setCollectionId("sm1");
        catalogCard.setCollectionName("Sun & Moon");
        catalogCard.setCardNumber("1");
        catalogCard.setRarity("Common");

        CardEntity cardEntity =
                new CardEntity();

        cardEntity.setExternalId("sm1-1");

        CardEntity savedCard =
                new CardEntity();

        savedCard.setId(10L);
        savedCard.setUser(user);
        savedCard.setExternalId("sm1-1");

        WishlistCardEntity wishlistCard =
                WishlistCardEntity.builder()
                        .user(user)
                        .externalId("sm1-1")
                        .name("Caterpie")
                        .priority(WishlistPriority.MEDIUM)
                        .build();

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository
                .findByUserAndExternalIdAndLanguageAndCondition(
                        user,
                        "sm1-1",
                        CardLanguage.ENGLISH,
                        CardCondition.NEAR_MINT
                ))
                .thenReturn(Optional.empty());

        when(pokemonCardCatalogService.findByExternalId(
                "sm1-1"
        )).thenReturn(catalogCard);

        when(cardMapper.toEntity(request))
                .thenReturn(cardEntity);

        when(cardRepository.save(cardEntity))
                .thenReturn(savedCard);

        when(wishlistCardRepository.findByUserAndExternalId(
                user,
                "sm1-1"
        )).thenReturn(Optional.of(wishlistCard));

        cardService.create(request);

        verify(wishlistCardRepository)
                .delete(wishlistCard);
    }

    @Test
    void shouldReturnCollectionChecklistWhenUserOwnsNoCards() {
        PokemonCardCatalogEntity caterpie =
                createCatalogCard(
                        "sm1-1",
                        "Caterpie",
                        "1",
                        "Common"
                );

        PokemonCardCatalogEntity metapod =
                createCatalogCard(
                        "sm1-2",
                        "Metapod",
                        "2",
                        "Uncommon"
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(
                        caterpie,
                        metapod
                )
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals(
                "sm1",
                result.collectionId()
        );

        assertEquals(
                "Sun & Moon",
                result.collectionName()
        );

        assertEquals(
                0L,
                result.ownedUniqueCards()
        );

        assertEquals(
                2L,
                result.totalCards()
        );

        assertEquals(
                0.0,
                result.completionPercentage()
        );

        assertEquals(
                2,
                result.cards().size()
        );

        assertFalse(
                result.cards().get(0).owned()
        );

        assertNull(
                result.cards().get(0).cardId()
        );

        assertFalse(
                result.cards().get(1).owned()
        );

        assertNull(
                result.cards().get(1).cardId()
        );

        verify(authenticatedUserService)
                .getAuthenticatedUser();

        verify(cardRepository)
                .findByUserAndCollectionId(
                        user,
                        "sm1"
                );

        verify(pokemonCardCatalogService)
                .findByCollectionId(
                        "sm1"
                );
    }

    @Test
    void shouldClassifyCardAsNumbered() {
        PokemonCardCatalogEntity numberedCard =
                createCatalogCard(
                        "sm1-149",
                        "Numbered Card",
                        "149",
                        "Rare"
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(numberedCard)
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals(
                CardCollectionSection.NUMBERED,
                result.cards().get(0).section()
        );
    }

    @Test
    void shouldClassifyCardAsAdditionalWhenNumberExceedsPrintedTotal() {
        PokemonCardCatalogEntity additionalCard =
                createCatalogCard(
                        "sm1-150",
                        "Additional Card",
                        "150",
                        "Rare"
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(additionalCard)
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals(
                CardCollectionSection.ADDITIONAL,
                result.cards().get(0).section()
        );
    }

    @Test
    void shouldClassifyCardAsAdditionalWhenCardNumberIsNonNumeric() {
        PokemonCardCatalogEntity additionalCard =
                createCatalogCard(
                        "sm1-TG01",
                        "Special Card",
                        "TG01",
                        "Rare"
                );

        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        when(cardRepository.findByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(wishlistCardRepository.findAllByUserAndCollectionId(
                user,
                "sm1"
        )).thenReturn(List.of());

        when(pokemonCardCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                List.of(additionalCard)
        );

        when(pokemonSetCatalogService.findByCollectionId(
                "sm1"
        )).thenReturn(
                Optional.of(createPokemonSetCatalog())
        );

        CollectionChecklistResponse result =
                cardService.getCollectionChecklist("sm1");

        assertEquals(
                CardCollectionSection.ADDITIONAL,
                result.cards().get(0).section()
        );
    }
}