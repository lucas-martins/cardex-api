package com.cardex.api.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AwesomeApiQuote(
        @JsonProperty("bid") String bid
) {
}
