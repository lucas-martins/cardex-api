package com.cardex.api.pokemon.controller;

import com.cardex.api.pokemon.response.PokemonCardSearchPageResponse;
import com.cardex.api.pokemon.response.PokemonCollectionResponse;
import com.cardex.api.pokemon.service.PokemonCardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon/cards")
@Validated
@RequiredArgsConstructor
public class PokemonCardController {

    private final PokemonCardService pokemonCardService;

    @GetMapping
    public ResponseEntity<PokemonCardSearchPageResponse> search(
            @RequestParam(required = false) String name,

            @RequestParam(required = false) String setId,

            @RequestParam(required = false) String number,

            @RequestParam(required = false) String rarity,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "Page must be at least 1")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size must not exceed 100")
            int size
    ) {
        boolean hasFilter =
                (name != null && !name.isBlank())
                        || (setId != null && !setId.isBlank())
                        || (number != null && !number.isBlank())
                        || (rarity != null && !rarity.isBlank());

        if (!hasFilter) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                pokemonCardService.search(
                        name,
                        setId,
                        number,
                        rarity,
                        page,
                        size
                )
        );
    }

    @GetMapping("/collections")
    public ResponseEntity<List<PokemonCollectionResponse>>
    findCollections() {
        return ResponseEntity.ok(
                pokemonCardService.findCollections()
        );
    }
}
