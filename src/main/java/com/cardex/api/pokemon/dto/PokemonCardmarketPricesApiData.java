package com.cardex.api.pokemon.dto;

import java.math.BigDecimal;

public record PokemonCardmarketPricesApiData(
        BigDecimal averageSellPrice,
        BigDecimal lowPrice,
        BigDecimal trendPrice
) {
}
