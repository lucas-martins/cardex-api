package com.cardex.api.pokemon.controller;

import com.cardex.api.pokemon.response.PokemonCardSearchResponse;
import com.cardex.api.pokemon.service.PokemonCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon/cards")
@RequiredArgsConstructor
public class PokemonCardController {

    private final PokemonCardService pokemonCardService;

    @GetMapping
    public ResponseEntity<List<PokemonCardSearchResponse>> searchByName(
            @RequestParam String name
    ) {
        List<PokemonCardSearchResponse> cards =
                pokemonCardService.searchByName(name);

        return ResponseEntity.ok(cards);
    }
}