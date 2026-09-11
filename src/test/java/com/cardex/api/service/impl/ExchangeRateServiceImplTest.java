package com.cardex.api.service.impl;

import com.cardex.api.config.properties.ExchangeRateProperties;
import com.cardex.api.exchange.dto.AwesomeApiExchangeResponse;
import com.cardex.api.exchange.dto.AwesomeApiQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

    @Mock
    private RestClient exchangeRateRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ExchangeRateServiceImpl exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService =
                new ExchangeRateServiceImpl(
                        exchangeRateRestClient,
                        new ExchangeRateProperties(
                                "https://economia.awesomeapi.com.br",
                                360
                        )
                );
    }

    @Test
    void shouldConvertUsdPreferredOverEur() {
        stubExchangeResponse(
                new AwesomeApiExchangeResponse(
                        new AwesomeApiQuote("5.00"),
                        new AwesomeApiQuote("5.50")
                )
        );

        assertEquals(
                new BigDecimal("10.00"),
                exchangeRateService.toBrl(
                        new BigDecimal("2.00"),
                        new BigDecimal("3.00")
                )
        );
    }

    @Test
    void shouldFallbackToEurWhenUsdIsMissing() {
        stubExchangeResponse(
                new AwesomeApiExchangeResponse(
                        new AwesomeApiQuote("5.00"),
                        new AwesomeApiQuote("5.50")
                )
        );

        assertEquals(
                new BigDecimal("11.00"),
                exchangeRateService.toBrl(
                        null,
                        new BigDecimal("2.00")
                )
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubExchangeResponse(
            AwesomeApiExchangeResponse response
    ) {
        when(exchangeRateRestClient.get())
                .thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(any(String.class)))
                .thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(AwesomeApiExchangeResponse.class))
                .thenReturn(response);
    }
}
