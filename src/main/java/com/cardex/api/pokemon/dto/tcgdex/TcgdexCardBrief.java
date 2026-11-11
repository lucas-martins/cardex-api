package com.cardex.api.pokemon.dto.tcgdex;

public record TcgdexCardBrief(
        String id,
        Object localId,
        String name,
        String image
) {
}
