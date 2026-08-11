package com.cardex.api.controller;

import com.cardex.api.dto.response.CardHistoryResponse;
import com.cardex.api.enumeration.CardHistoryAction;
import com.cardex.api.service.CardHistoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card-history")
@RequiredArgsConstructor
@Validated
public class CardHistoryController {

    private final CardHistoryService cardHistoryService;

    @GetMapping
    public ResponseEntity<Page<CardHistoryResponse>> findAll(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size must not exceed 100")
            int size,

            @RequestParam(required = false)
            CardHistoryAction action
    ) {
        return ResponseEntity.ok(
                cardHistoryService.findAll(
                        page,
                        size,
                        action
                )
        );
    }
}