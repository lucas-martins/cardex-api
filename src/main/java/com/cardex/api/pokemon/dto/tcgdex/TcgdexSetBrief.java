package com.cardex.api.pokemon.dto.tcgdex;

public record TcgdexSetBrief(
        String id,
        String name,
        TcgdexCardCount cardCount,
        TcgdexSerieBrief serie
) {
}
