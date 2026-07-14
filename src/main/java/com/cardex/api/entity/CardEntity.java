package com.cardex.api.entity;

import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardEntity extends BaseEntity {

    @Column(name = "external_id", length = 50)
    private String externalId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "collection_name", nullable = false, length = 150)
    private String collectionName;

    @Column(name = "card_number", nullable = false, length = 30)
    private String cardNumber;

    @Column(length = 100)
    private String rarity;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CardLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", length = 30)
    private CardCondition condition;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String notes;
}