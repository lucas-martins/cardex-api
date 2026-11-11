package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexCardDetail(
        String id,
        Object localId,
        String name,
        String rarity,
        String image,
        TcgdexSetBrief set,
        TcgdexPricing pricing
) {
}
