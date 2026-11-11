package com.cardex.api.pokemon.dto.tcgdex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TcgdexTcgPlayerPricing(
        String unit,
        String updated,
        TcgdexVariantPrice normal,
        TcgdexVariantPrice holofoil,
        @JsonProperty("reverse-holofoil")
        TcgdexVariantPrice reverseHolofoil,
        TcgdexVariantPrice reverse,
        @JsonProperty("1st-edition")
        TcgdexVariantPrice firstEdition,
        @JsonProperty("1st-edition-holofoil")
        TcgdexVariantPrice firstEditionHolofoil,
        TcgdexVariantPrice unlimited,
        @JsonProperty("unlimited-holofoil")
        TcgdexVariantPrice unlimitedHolofoil
) {
}
