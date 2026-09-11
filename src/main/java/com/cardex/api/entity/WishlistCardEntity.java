package com.cardex.api.entity;

import com.cardex.api.enumeration.WishlistPriority;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "wishlist_cards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wishlist_user_external_id",
                        columnNames = {
                                "user_id",
                                "external_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WishlistCardEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "collection_id")
    private String collectionId;

    @Column(name = "collection_name")
    private String collectionName;

    private String series;

    private String rarity;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false
    )
    private WishlistPriority priority =
            WishlistPriority.MEDIUM;

    @Column(length = 1000)
    private String notes;

    @Column(name = "store_url", length = 1000)
    private String storeUrl;

    @Column(name = "target_price_usd", precision = 12, scale = 2)
    private java.math.BigDecimal targetPriceUsd;
}