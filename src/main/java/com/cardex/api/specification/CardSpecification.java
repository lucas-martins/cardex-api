package com.cardex.api.specification;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.entity.PokemonCardCatalogEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.entity.UserEntity;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public final class CardSpecification {

    private CardSpecification() {
    }

    public static Specification<CardEntity> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<CardEntity> languageEquals(
            CardLanguage language
    ) {
        return (root, query, criteriaBuilder) -> {
            if (language == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("language"),
                    language
            );
        };
    }

    public static Specification<CardEntity> conditionEquals(
            CardCondition condition
    ) {
        return (root, query, criteriaBuilder) -> {
            if (condition == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("condition"),
                    condition
            );
        };
    }

    public static Specification<CardEntity> favoriteEquals(Boolean favorite) {
        return (root, query, criteriaBuilder) -> {
            if (favorite == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("favorite"),
                    favorite
            );
        };
    }

    public static Specification<CardEntity> userEquals(UserEntity user) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("user"),
                        user
                );
    }
    public static Specification<CardEntity> collectionContains(
            String collection
    ) {
        return (root, query, criteriaBuilder) -> {
            if (collection == null || collection.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("collectionName")),
                    "%" + collection.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<CardEntity> rarityContains(
            String rarity
    ) {
        return (root, query, criteriaBuilder) -> {
            if (rarity == null || rarity.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("rarity")),
                    "%" + rarity.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<CardEntity> numberContains(
            String number
    ) {
        return (root, query, criteriaBuilder) -> {
            if (number == null || number.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("cardNumber")),
                    "%" + number.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<CardEntity> typeContains(
            String type
    ) {
        return (root, query, criteriaBuilder) -> {
            if (type == null || type.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Subquery<String> subquery = query.subquery(String.class);
            Root<PokemonCardCatalogEntity> catalog =
                    subquery.from(PokemonCardCatalogEntity.class);

            subquery.select(catalog.get("externalId"))
                    .where(
                            criteriaBuilder.equal(
                                    catalog.get("externalId"),
                                    root.get("externalId")
                            ),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            catalog.get("types")
                                    ),
                                    "%"
                                            + type.trim().toLowerCase()
                                            + "%"
                            )
                    );

            return criteriaBuilder.exists(subquery);
        };
    }
}