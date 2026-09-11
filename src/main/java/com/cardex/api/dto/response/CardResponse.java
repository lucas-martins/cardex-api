package com.cardex.api.dto.response;

import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class CardResponse {

    private Long id;
    private String externalId;
    private String name;
    private String collectionName;
    private String cardNumber;
    private String rarity;
    private Integer quantity;
    private CardLanguage language;
    private CardCondition condition;
    private String imageUrl;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean favorite;
    private String collectionId;
    private Integer collectionTotal;
    private BigDecimal marketPriceUsd;
    private BigDecimal marketPriceEur;
    private BigDecimal marketPriceBrl;
    private BigDecimal estimatedValueUsd;
    private BigDecimal estimatedValueEur;
    private BigDecimal estimatedValueBrl;
}