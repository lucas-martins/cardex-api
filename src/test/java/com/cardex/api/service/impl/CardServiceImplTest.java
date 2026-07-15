package com.cardex.api.service.impl;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardQuantityRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.exception.CardNotFoundException;
import com.cardex.api.exception.PokemonCardNotFoundException;
import com.cardex.api.mapper.CardMapper;
import com.cardex.api.pokemon.client.PokemonTcgClient;
import com.cardex.api.pokemon.dto.PokemonCardApiData;
import com.cardex.api.pokemon.dto.PokemonCardApiSingleResponse;
import com.cardex.api.pokemon.dto.PokemonCardImagesApiData;
import com.cardex.api.pokemon.dto.PokemonSetApiData;
import com.cardex.api.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private PokemonTcgClient pokemonTcgClient;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardEntity cardEntity;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        cardEntity = new CardEntity();
        cardEntity.setName("Decidueye-GX");

        cardResponse = CardResponse.builder()
                .id(1L)
                .name("Decidueye-GX")
                .build();
    }

    @Test
    void shouldFindCardById() {
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(cardEntity));

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(cardResponse);

        CardResponse result = cardService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Decidueye-GX", result.getName());

        verify(cardRepository).findById(1L);
        verify(cardMapper).toResponse(cardEntity);
    }

    @Test
    void shouldThrowExceptionWhenCardDoesNotExist() {
        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> cardService.findById(999L)
        );

        assertEquals(
                "Card not found for ID: 999",
                exception.getMessage()
        );

        verify(cardRepository).findById(999L);
    }

    @Test
    void shouldUpdateCardQuantity() {
        UpdateCardQuantityRequest request =
                new UpdateCardQuantityRequest(3);

        cardEntity.setQuantity(1);

        CardResponse updatedResponse = CardResponse.builder()
                .id(1L)
                .name("Decidueye-GX")
                .quantity(3)
                .build();

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(cardEntity));

        when(cardRepository.save(cardEntity))
                .thenReturn(cardEntity);

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(updatedResponse);

        CardResponse result =
                cardService.updateQuantity(1L, request);

        assertEquals(3, cardEntity.getQuantity());
        assertEquals(3, result.getQuantity());

        verify(cardRepository).findById(1L);
        verify(cardRepository).save(cardEntity);
        verify(cardMapper).toResponse(cardEntity);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingQuantityOfNonexistentCard() {
        UpdateCardQuantityRequest request =
                new UpdateCardQuantityRequest(3);

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> cardService.updateQuantity(999L, request)
        );

        assertEquals(
                "Card not found for ID: 999",
                exception.getMessage()
        );

        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).save(cardEntity);
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

        when(cardRepository.findByExternalIdAndLanguageAndCondition(
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(Optional.of(cardEntity));

        when(cardRepository.save(cardEntity))
                .thenReturn(cardEntity);

        when(cardMapper.toResponse(cardEntity))
                .thenReturn(updatedResponse);

        CardResponse result = cardService.create(request);

        assertEquals(5, cardEntity.getQuantity());
        assertEquals(5, result.getQuantity());

        verify(cardRepository).findByExternalIdAndLanguageAndCondition(
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        );
        verify(cardRepository).save(cardEntity);
        verify(cardMapper).toResponse(cardEntity);
        verifyNoInteractions(pokemonTcgClient);
    }

    @Test
    void shouldCreateNewCardUsingPokemonTcgApiData() {
        CreateCardRequest request = new CreateCardRequest();
        request.setExternalId("sm1-12");
        request.setQuantity(1);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);
        request.setNotes("First card");

        PokemonSetApiData set = new PokemonSetApiData(
                "sm1",
                "Sun & Moon",
                "Sun & Moon",
                149,
                163
        );

        PokemonCardImagesApiData images =
                new PokemonCardImagesApiData(
                        "https://images.pokemontcg.io/sm1/12.png",
                        "https://images.pokemontcg.io/sm1/12_hires.png"
                );

        PokemonCardApiData pokemonCard = new PokemonCardApiData(
                "sm1-12",
                "Decidueye-GX",
                "12",
                "Rare Holo GX",
                set,
                images
        );

        PokemonCardApiSingleResponse apiResponse =
                new PokemonCardApiSingleResponse(pokemonCard);

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
                .imageUrl("https://images.pokemontcg.io/sm1/12_hires.png")
                .notes("First card")
                .build();

        when(cardRepository.findByExternalIdAndLanguageAndCondition(
                "sm1-12",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(Optional.empty());

        when(pokemonTcgClient.findById("sm1-12"))
                .thenReturn(apiResponse);

        when(cardMapper.toEntity(request))
                .thenReturn(newCardEntity);

        when(cardRepository.save(newCardEntity))
                .thenReturn(newCardEntity);

        when(cardMapper.toResponse(newCardEntity))
                .thenReturn(savedResponse);

        CardResponse result = cardService.create(request);

        assertEquals("Decidueye-GX", newCardEntity.getName());
        assertEquals("Sun & Moon", newCardEntity.getCollectionName());
        assertEquals("12", newCardEntity.getCardNumber());
        assertEquals("Rare Holo GX", newCardEntity.getRarity());
        assertEquals(
                "https://images.pokemontcg.io/sm1/12_hires.png",
                newCardEntity.getImageUrl()
        );

        assertEquals(1L, result.getId());
        assertEquals("Decidueye-GX", result.getName());

        verify(pokemonTcgClient).findById("sm1-12");
        verify(cardMapper).toEntity(request);
        verify(cardRepository).save(newCardEntity);
        verify(cardMapper).toResponse(newCardEntity);
    }

    @Test
    void shouldThrowExceptionWhenPokemonCardIsNotFound() {
        CreateCardRequest request = new CreateCardRequest();
        request.setExternalId("invalid-id");
        request.setQuantity(1);
        request.setLanguage(CardLanguage.ENGLISH);
        request.setCondition(CardCondition.NEAR_MINT);

        when(cardRepository.findByExternalIdAndLanguageAndCondition(
                "invalid-id",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        )).thenReturn(Optional.empty());

        when(pokemonTcgClient.findById("invalid-id"))
                .thenReturn(null);

        PokemonCardNotFoundException exception = assertThrows(
                PokemonCardNotFoundException.class,
                () -> cardService.create(request)
        );

        assertEquals(
                "Pokemon card not found for external ID: invalid-id",
                exception.getMessage()
        );

        verify(cardRepository).findByExternalIdAndLanguageAndCondition(
                "invalid-id",
                CardLanguage.ENGLISH,
                CardCondition.NEAR_MINT
        );
        verify(pokemonTcgClient).findById("invalid-id");
        verify(cardRepository, never()).save(any(CardEntity.class));
    }
}