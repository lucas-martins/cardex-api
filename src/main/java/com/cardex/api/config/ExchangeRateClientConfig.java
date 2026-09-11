package com.cardex.api.config;

import com.cardex.api.config.properties.ExchangeRateProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ExchangeRateProperties.class)
public class ExchangeRateClientConfig {

    @Bean
    public RestClient exchangeRateRestClient(
            ExchangeRateProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        "application/json"
                )
                .build();
    }
}
