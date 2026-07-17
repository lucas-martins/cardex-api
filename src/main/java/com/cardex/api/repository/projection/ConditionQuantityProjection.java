package com.cardex.api.repository.projection;

import com.cardex.api.enumeration.CardCondition;

public interface ConditionQuantityProjection {

    CardCondition getCondition();

    Long getQuantity();
}