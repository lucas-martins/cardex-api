package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexCardDetail(
        String id,
        Object localId,
        String name,
        String rarity,
        List<String> types,
        String image,
        TcgdexSetBrief set,
        TcgdexPricing pricing
) {
}
