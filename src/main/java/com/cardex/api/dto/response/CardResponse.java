package com.cardex.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardResponse {

    private Long id;
    private String externalId;
    private String name;
    private String collectionName;
    private String cardNumber;
    private String rarity;
    private Integer quantity;
    private String language;
    private String condition;
    private String imageUrl;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}