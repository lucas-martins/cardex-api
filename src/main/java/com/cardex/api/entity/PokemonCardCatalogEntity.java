package com.cardex.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "pokemon_card_catalog",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pokemon_card_catalog_external_id",
                        columnNames = "external_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonCardCatalogEntity extends BaseEntity {

    @Column(
            name = "external_id",
            nullable = false,
            length = 50
    )
    private String externalId;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "card_number",
            nullable = false,
            length = 30
    )
    private String cardNumber;

    @Column(length = 100)
    private String rarity;

    @Column(
            name = "collection_id",
            nullable = false,
            length = 50
    )
    private String collectionId;

    @Column(
            name = "collection_name",
            nullable = false,
            length = 150
    )
    private String collectionName;

    @Column(
            name = "collection_series",
            length = 150
    )
    private String collectionSeries;

    @Column(name = "collection_total")
    private Integer collectionTotal;

    @Column(
            name = "image_url",
            length = 1000
    )
    private String imageUrl;
}