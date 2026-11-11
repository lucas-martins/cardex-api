package com.cardex.api.pokemon.dto.tcgdex;

import java.util.List;

public record TcgdexSerieDetail(
        String id,
        String name,
        List<TcgdexSetBrief> sets
) {
}
