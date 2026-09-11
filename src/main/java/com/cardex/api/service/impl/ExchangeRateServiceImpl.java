package com.cardex.api.service.impl;

import com.cardex.api.config.properties.ExchangeRateProperties;
import com.cardex.api.exchange.dto.AwesomeApiExchangeResponse;
import com.cardex.api.exchange.dto.AwesomeApiQuote;
import com.cardex.api.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl
        implements ExchangeRateService {

    private static final int RATE_SCALE = 6;

    private static final int MONEY_SCALE = 2;

    private static final BigDecimal FALLBACK_USD_TO_BRL =
            new BigDecimal("5.00");

    private static final BigDecimal FALLBACK_EUR_TO_BRL =
            new BigDecimal("5.50");

    private final RestClient exchangeRateRestClient;

    private final ExchangeRateProperties properties;

    private final AtomicReference<CachedRates> cachedRates =
            new AtomicReference<>();

    @Override
    public BigDecimal getUsdToBrlRate() {
        return currentRates().usdToBrl();
    }

    @Override
    public BigDecimal getEurToBrlRate() {
        return currentRates().eurToBrl();
    }

    @Override
    public BigDecimal toBrl(
            BigDecimal amountUsd,
            BigDecimal amountEur
    ) {
        if (amountUsd != null) {
            return amountUsd
                    .multiply(getUsdToBrlRate())
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        if (amountEur != null) {
            return amountEur
                    .multiply(getEurToBrlRate())
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        return null;
    }

    private CachedRates currentRates() {
        CachedRates cached = cachedRates.get();

        if (cached != null && !cached.isExpired(
                properties.cacheMinutes()
        )) {
            return cached;
        }

        synchronized (this) {
            cached = cachedRates.get();

            if (cached != null && !cached.isExpired(
                    properties.cacheMinutes()
            )) {
                return cached;
            }

            CachedRates refreshed = fetchRates();
            cachedRates.set(refreshed);
            return refreshed;
        }
    }

    private CachedRates fetchRates() {
        try {
            AwesomeApiExchangeResponse response =
                    exchangeRateRestClient
                            .get()
                            .uri("/json/last/USD-BRL,EUR-BRL")
                            .retrieve()
                            .body(AwesomeApiExchangeResponse.class);

            BigDecimal usdToBrl =
                    parseRate(
                            response != null
                                    ? response.usdBrl()
                                    : null
                    );

            BigDecimal eurToBrl =
                    parseRate(
                            response != null
                                    ? response.eurBrl()
                                    : null
                    );

            return new CachedRates(
                    usdToBrl != null
                            ? usdToBrl
                            : FALLBACK_USD_TO_BRL,
                    eurToBrl != null
                            ? eurToBrl
                            : FALLBACK_EUR_TO_BRL,
                    Instant.now()
            );
        } catch (RestClientException exception) {
            CachedRates previous = cachedRates.get();

            if (previous != null) {
                return previous;
            }

            return new CachedRates(
                    FALLBACK_USD_TO_BRL,
                    FALLBACK_EUR_TO_BRL,
                    Instant.now()
            );
        }
    }

    private BigDecimal parseRate(AwesomeApiQuote quote) {
        if (quote == null
                || quote.bid() == null
                || quote.bid().isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(quote.bid())
                    .setScale(RATE_SCALE, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record CachedRates(
            BigDecimal usdToBrl,
            BigDecimal eurToBrl,
            Instant fetchedAt
    ) {
        private boolean isExpired(long cacheMinutes) {
            return fetchedAt
                    .plusSeconds(Math.max(cacheMinutes, 1) * 60)
                    .isBefore(Instant.now());
        }
    }
}
