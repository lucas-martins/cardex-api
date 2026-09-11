package com.cardex.api.repository.projection;

import java.math.BigDecimal;

public interface CollectionValueByCollectionProjection {

    String getCollectionId();

    String getCollectionName();

    BigDecimal getEstimatedValueUsd();

    BigDecimal getEstimatedValueEur();

    BigDecimal getEstimatedValueBrl();
}
