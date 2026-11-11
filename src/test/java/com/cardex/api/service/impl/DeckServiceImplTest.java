package com.cardex.api.service.impl;

import com.cardex.api.deck.DeckCardResolver;
import com.cardex.api.deck.ResolvedDeckCard;
import com.cardex.api.dto.deck.CreateDeckRequest;
import com.cardex.api.dto.deck.DeckCardInput;
import com.cardex.api.dto.deck.DeckResponse;
import com.cardex.api.dto.deck.ParseDeckListRequest;
import com.cardex.api.entity.DeckEntity;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.enumeration.DeckSection;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.repository.DeckCardRepository;
import com.cardex.api.repository.DeckRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.PokemonCardCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckCardRepository deckCardRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private DeckCardResolver deckCardResolver;

    @Mock
    private PokemonCardCatalogService pokemonCardCatalogService;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
    }

    @Test
    void shouldParseDeckListIntoResolvedAndUnresolved() {
        when(deckCardResolver.resolveList(eq("list"), eq(user)))
                .thenReturn(
                        List.of(
                                new ResolvedDeckCard(
                                        true,
                                        1,
                                        "2 Cosmog SUM 64",
                                        DeckSection.POKEMON,
                                        2,
                                        "Cosmog",
                                        "SUM",
                                        "64",
                                        "sm1-64",
                                        "Cosmog",
                                        "sm1",
                                        "Sun & Moon",
                                        "image",
                                        1
                                ),
                                new ResolvedDeckCard(
                                        false,
                                        2,
                                        "1 Fake Card XYZ 1",
                                        DeckSection.TRAINER,
                                        1,
                                        "Fake Card",
                                        "XYZ",
                                        "1",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        0
                                )
                        )
                );

        var response = deckService.parse(new ParseDeckListRequest("list"));

        assertEquals(2, response.totalLines());
        assertEquals(1, response.resolvedCount());
        assertEquals(1, response.unresolvedCount());
        assertEquals("sm1-64", response.resolvedCards().getFirst().externalId());
        assertEquals(1, response.resolvedCards().getFirst().ownedQuantity());
    }

    @Test
    void shouldCreateDeckFromResolvedCards() {
        PokemonCardCatalogEntity catalog = new PokemonCardCatalogEntity();
        catalog.setExternalId("sm1-64");
        catalog.setName("Cosmog");
        catalog.setCardNumber("64");
        catalog.setCollectionId("sm1");
        catalog.setCollectionName("Sun & Moon");
        catalog.setImageUrl("image");

        when(pokemonCardCatalogService.findByExternalId("sm1-64"))
                .thenReturn(catalog);
        when(cardRepository.sumQuantityByUserAndExternalIdIn(eq(user), anyList()))
                .thenReturn(List.of());
        when(deckRepository.save(any(DeckEntity.class)))
                .thenAnswer(invocation -> {
                    DeckEntity deck = invocation.getArgument(0);
                    deck.setId(10L);
                    return deck;
                });

        DeckResponse response = deckService.create(
                new CreateDeckRequest(
                        "Test deck",
                        "notes",
                        List.of(
                                new DeckCardInput(
                                        "sm1-64",
                                        DeckSection.POKEMON,
                                        2
                                )
                        )
                )
        );

        ArgumentCaptor<DeckEntity> captor =
                ArgumentCaptor.forClass(DeckEntity.class);
        verify(deckRepository).save(captor.capture());

        assertEquals("Test deck", captor.getValue().getName());
        assertEquals(1, captor.getValue().getCards().size());
        assertEquals(2, captor.getValue().getCards().getFirst().getQuantity());
        assertEquals(10L, response.id());
        assertEquals(2, response.totalCards());
    }
}
