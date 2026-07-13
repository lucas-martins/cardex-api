package com.cardex.api.pokemon.dto;

public record PokemonCardApiData(
        String id,
        String name,
        String number,
        String rarity,
        PokemonSetApiData set,
        PokemonCardImagesApiData images
) {
}