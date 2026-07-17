package com.cardex.api.controller;

import com.cardex.api.dto.wishlist.WishlistCardRequest;
import com.cardex.api.dto.wishlist.WishlistCardResponse;
import com.cardex.api.service.WishlistCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistCardController {

    private final WishlistCardService wishlistCardService;

    @PostMapping
    public ResponseEntity<WishlistCardResponse> create(
            @Valid @RequestBody WishlistCardRequest request
    ) {
        WishlistCardResponse response =
                wishlistCardService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WishlistCardResponse>> findAll() {
        return ResponseEntity.ok(
                wishlistCardService.findAll()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        wishlistCardService.delete(id);

        return ResponseEntity.noContent().build();
    }
}