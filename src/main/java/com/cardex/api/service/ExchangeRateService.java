package com.cardex.api.service;

import java.math.BigDecimal;

public interface ExchangeRateService {

    BigDecimal getUsdToBrlRate();

    BigDecimal getEurToBrlRate();

    BigDecimal toBrl(
            BigDecimal amountUsd,
            BigDecimal amountEur
    );
}
