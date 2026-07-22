package com.cardex.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cardex.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMinutes
) {
}