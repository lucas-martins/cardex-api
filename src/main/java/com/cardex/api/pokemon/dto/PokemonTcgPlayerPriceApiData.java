package com.cardex.api.pokemon.dto;

import java.math.BigDecimal;

public record PokemonTcgPlayerPriceApiData(
        BigDecimal low,
        BigDecimal mid,
        BigDecimal high,
        BigDecimal market,
        BigDecimal directLow
) {
}
