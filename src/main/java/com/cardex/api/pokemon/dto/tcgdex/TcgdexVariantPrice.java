package com.cardex.api.pokemon.dto.tcgdex;

import java.math.BigDecimal;

public record TcgdexVariantPrice(
        BigDecimal lowPrice,
        BigDecimal midPrice,
        BigDecimal highPrice,
        BigDecimal marketPrice,
        BigDecimal directLowPrice
) {
}
