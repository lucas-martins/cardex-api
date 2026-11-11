package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexSetAbbreviation(
        String official
) {
}
