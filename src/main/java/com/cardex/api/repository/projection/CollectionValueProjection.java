package com.cardex.api.repository.projection;

import java.math.BigDecimal;

public interface CollectionValueProjection {

    BigDecimal getEstimatedValueUsd();

    BigDecimal getEstimatedValueEur();

    BigDecimal getEstimatedValueBrl();

    Long getPricedCopies();

    Long getUnpricedCopies();
}
