package com.cardex.api.specification;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
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
}