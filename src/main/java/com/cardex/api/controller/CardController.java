package com.cardex.api.controller;

import com.cardex.api.dto.request.CreateCardRequest;
import com.cardex.api.dto.request.UpdateCardFavoriteRequest;
import com.cardex.api.dto.request.UpdateCardRequest;
import com.cardex.api.dto.response.CardResponse;
import com.cardex.api.dto.response.CollectionSummaryResponse;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> create(
            @Valid @RequestBody CreateCardRequest request
    ) {
        CardResponse response = cardService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> findAll(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size must not exceed 100")
            int size,

            @RequestParam(required = false)
            String name,

            @RequestParam(required = false)
            CardLanguage language,

            @RequestParam(required = false)
            CardCondition condition,

            @RequestParam(required = false)
            Boolean favorite,

            @RequestParam(defaultValue = "name,asc")
            String sort
    ) {
        return ResponseEntity.ok(
                cardService.findAll(
                        page,
                        size,
                        name,
                        language,
                        condition,
                        favorite,
                        sort
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cardService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardRequest request
    ) {
        return ResponseEntity.ok(cardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        cardService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<CollectionSummaryResponse> getCollectionSummary() {
        return ResponseEntity.ok(
                cardService.getCollectionSummary()
        );
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<CardResponse> updateFavorite(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardFavoriteRequest request
    ) {
        return ResponseEntity.ok(
                cardService.updateFavorite(id, request)
        );
    }
}