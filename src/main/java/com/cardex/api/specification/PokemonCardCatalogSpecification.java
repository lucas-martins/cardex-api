package com.cardex.api.specification;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import org.springframework.data.jpa.domain.Specification;

public final class PokemonCardCatalogSpecification {

    private PokemonCardCatalogSpecification() {
    }

    public static Specification<PokemonCardCatalogEntity> nameContains(
            String name
    ) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) {
                return builder.conjunction();
            }

            return builder.like(
                    builder.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<PokemonCardCatalogEntity> collectionIdEquals(
            String collectionId
    ) {
        return (root, query, builder) -> {
            if (collectionId == null || collectionId.isBlank()) {
                return builder.conjunction();
            }

            return builder.equal(
                    root.get("collectionId"),
                    collectionId.trim()
            );
        };
    }

    public static Specification<PokemonCardCatalogEntity> numberContains(
            String number
    ) {
        return (root, query, builder) -> {
            if (number == null || number.isBlank()) {
                return builder.conjunction();
            }

            return builder.like(
                    builder.lower(root.get("cardNumber")),
                    "%" + number.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<PokemonCardCatalogEntity> rarityContains(
            String rarity
    ) {
        return (root, query, builder) -> {
            if (rarity == null || rarity.isBlank()) {
                return builder.conjunction();
            }

            return builder.like(
                    builder.lower(root.get("rarity")),
                    "%" + rarity.trim().toLowerCase() + "%"
            );
        };
    }
}
