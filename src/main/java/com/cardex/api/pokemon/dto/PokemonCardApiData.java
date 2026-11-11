package com.cardex.api.pokemon.dto;

import java.util.List;

public record PokemonCardApiData(
        String id,
        String name,
        String number,
        String rarity,
        List<String> types,
        PokemonSetApiData set,
        PokemonCardImagesApiData images,
        PokemonTcgPlayerApiData tcgplayer,
        PokemonCardmarketApiData cardmarket
) {
}
