package com.cardex.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange-rate")
public record ExchangeRateProperties(
        String baseUrl,
        long cacheMinutes
) {
}
