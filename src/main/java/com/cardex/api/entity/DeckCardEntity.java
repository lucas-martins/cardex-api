package com.cardex.api.entity;

import com.cardex.api.enumeration.DeckSection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "deck_cards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_deck_cards_deck_external_id",
                        columnNames = {
                                "deck_id",
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
public class DeckCardEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "card_number", length = 30)
    private String cardNumber;

    @Column(name = "collection_id", length = 50)
    private String collectionId;

    @Column(name = "collection_name", length = 150)
    private String collectionName;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeckSection section;

    @Column(nullable = false)
    private Integer quantity;
}
