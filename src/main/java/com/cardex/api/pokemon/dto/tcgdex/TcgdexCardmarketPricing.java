package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexCardmarketPricing(
        String unit,
        String updated,
        BigDecimal avg,
        BigDecimal low,
        BigDecimal trend
) {
}
