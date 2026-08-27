package com.cardex.api.controller;

import com.cardex.api.dto.request.CreateCollectionGoalRequest;
import com.cardex.api.dto.request.UpdateCollectionGoalRequest;
import com.cardex.api.dto.response.UserCollectionGoalResponse;
import com.cardex.api.service.CollectionGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/collection-goals")
@RequiredArgsConstructor
public class CollectionGoalController {

    private final CollectionGoalService collectionGoalService;

    @PostMapping
    public ResponseEntity<UserCollectionGoalResponse> create(
            @Valid @RequestBody CreateCollectionGoalRequest request
    ) {
        UserCollectionGoalResponse response =
                collectionGoalService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserCollectionGoalResponse>> findAll() {
        return ResponseEntity.ok(
                collectionGoalService.findAll()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserCollectionGoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollectionGoalRequest request
    ) {
        return ResponseEntity.ok(
                collectionGoalService.update(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        collectionGoalService.delete(id);

        return ResponseEntity.noContent().build();
    }
}