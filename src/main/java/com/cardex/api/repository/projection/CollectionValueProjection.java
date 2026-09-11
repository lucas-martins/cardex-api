package com.cardex.api.repository.projection;

import java.math.BigDecimal;

public interface CollectionValueProjection {

    BigDecimal getEstimatedValueUsd();

    BigDecimal getEstimatedValueEur();

    Long getPricedCopies();

    Long getUnpricedCopies();
}
