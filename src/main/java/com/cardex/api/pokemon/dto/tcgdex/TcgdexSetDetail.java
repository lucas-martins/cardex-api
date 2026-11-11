package com.cardex.api.pokemon.dto.tcgdex;

import java.util.List;

public record TcgdexSetDetail(
        String id,
        String name,
        TcgdexCardCount cardCount,
        TcgdexSerieBrief serie,
        List<TcgdexCardBrief> cards
) {
}
