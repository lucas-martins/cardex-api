package com.cardex.api.repository.projection;

import com.cardex.api.enumeration.CardLanguage;

public interface LanguageQuantityProjection {

    CardLanguage getLanguage();

    Long getQuantity();
}