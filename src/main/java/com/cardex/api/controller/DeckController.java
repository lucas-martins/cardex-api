package com.cardex.api.controller;

import com.cardex.api.dto.deck.AddDeckCardRequest;
import com.cardex.api.dto.deck.CreateDeckRequest;
import com.cardex.api.dto.deck.DeckParseResponse;
import com.cardex.api.dto.deck.DeckResponse;
import com.cardex.api.dto.deck.DeckSummaryResponse;
import com.cardex.api.dto.deck.ParseDeckListRequest;
import com.cardex.api.dto.deck.ReplaceDeckCardsRequest;
import com.cardex.api.dto.deck.UpdateDeckRequest;
import com.cardex.api.service.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @PostMapping("/parse")
    public ResponseEntity<DeckParseResponse> parse(
            @Valid @RequestBody ParseDeckListRequest request
    ) {
        return ResponseEntity.ok(deckService.parse(request));
    }

    @GetMapping
    public ResponseEntity<List<DeckSummaryResponse>> findAll() {
        return ResponseEntity.ok(deckService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DeckResponse> create(
            @Valid @RequestBody CreateDeckRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deckService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeckResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeckRequest request
    ) {
        return ResponseEntity.ok(deckService.update(id, request));
    }

    @PutMapping("/{id}/cards")
    public ResponseEntity<DeckResponse> replaceCards(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceDeckCardsRequest request
    ) {
        return ResponseEntity.ok(deckService.replaceCards(id, request));
    }

    @PostMapping("/{id}/cards")
    public ResponseEntity<DeckResponse> addCard(
            @PathVariable Long id,
            @Valid @RequestBody AddDeckCardRequest request
    ) {
        return ResponseEntity.ok(deckService.addCard(id, request));
    }

    @DeleteMapping("/{id}/cards/{cardId}")
    public ResponseEntity<DeckResponse> removeCard(
            @PathVariable Long id,
            @PathVariable Long cardId
    ) {
        return ResponseEntity.ok(deckService.removeCard(id, cardId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deckService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
