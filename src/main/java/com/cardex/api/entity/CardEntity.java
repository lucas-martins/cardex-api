package com.cardex.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardEntity extends BaseEntity {

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

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String notes;
}