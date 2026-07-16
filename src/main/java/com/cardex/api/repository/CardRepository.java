package com.cardex.api.repository;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends
        JpaRepository<CardEntity, Long>,
        JpaSpecificationExecutor<CardEntity> {

    Optional<CardEntity> findByExternalIdAndLanguageAndCondition(
            String externalId,
            CardLanguage language,
            CardCondition condition
    );

    @Query("""
        select coalesce(sum(card.quantity), 0)
        from CardEntity card
        """)
    Long sumTotalQuantity();

    @Query("""
        select count(distinct card.language)
        from CardEntity card
        """)
    long countDifferentLanguages();

    @Query("""
        select count(distinct card.collectionName)
        from CardEntity card
        where card.collectionName is not null
        """)
    long countDifferentCollections();

    Optional<CardEntity> findFirstByOrderByQuantityDescCreatedAtDesc();
}