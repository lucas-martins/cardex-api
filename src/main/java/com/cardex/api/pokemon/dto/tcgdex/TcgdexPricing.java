package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexPricing(
        TcgdexTcgPlayerPricing tcgplayer,
        TcgdexCardmarketPricing cardmarket
) {
}
