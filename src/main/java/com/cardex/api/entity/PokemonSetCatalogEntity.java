package com.cardex.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pokemon_set_catalog",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pokemon_set_catalog_collection_id",
                        columnNames = "collection_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonSetCatalogEntity extends BaseEntity {

    @Column(
            name = "collection_id",
            nullable = false,
            length = 50
    )
    private String collectionId;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            length = 150
    )
    private String series;

    @Column(
            name = "printed_total"
    )
    private Integer printedTotal;

    @Column(
            name = "total"
    )
    private Integer total;

    @Column(
            name = "last_synced_at",
            nullable = false
    )
    private LocalDateTime lastSyncedAt;
}