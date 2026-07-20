package com.cardex.api.repository;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.repository.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
    select
        card.collectionName as name,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.collectionName is not null
    group by card.collectionName
    order by sum(card.quantity) desc, card.collectionName asc
    """)
    List<CollectionQuantityProjection> findQuantityGroupedByCollection();

    @Query("""
    select
        card.language as language,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.language is not null
    group by card.language
    order by sum(card.quantity) desc, card.language asc
    """)
    List<LanguageQuantityProjection> findQuantityGroupedByLanguage();

    @Query("""
    select
        card.condition as condition,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.condition is not null
    group by card.condition
    order by sum(card.quantity) desc, card.condition asc
    """)
    List<ConditionQuantityProjection> findQuantityGroupedByCondition();

    @Query("""
    select
        card.rarity as rarity,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.rarity is not null
      and trim(card.rarity) <> ''
    group by card.rarity
    order by sum(card.quantity) desc, card.rarity asc
    """)
    List<RarityQuantityProjection> findQuantityGroupedByRarity();

    boolean existsByFavoriteTrue();

    @Query("""
    select count(card)
    from CardEntity card
    where card.rarity is not null
      and (
          lower(card.rarity) like '%rare%'
          or lower(card.rarity) like '%secret%'
          or lower(card.rarity) like '%ultra%'
          or lower(card.rarity) like '%illustration%'
      )
    """)
    long countRareCards();

    @Query("""
    select
        card.collectionId as collectionId,
        card.collectionName as collectionName,
        card.collectionTotal as collectionTotal,
        count(distinct card.externalId) as ownedCards
    from CardEntity card
    where card.collectionId is not null
      and card.collectionTotal is not null
    group by
        card.collectionId,
        card.collectionName,
        card.collectionTotal
    order by count(distinct card.externalId) desc,
             card.collectionName asc
    """)
    List<CollectionProgressProjection> findCollectionProgress();

    List<CardEntity> findByCollectionId(String collectionId);

    boolean existsByExternalIdAndLanguageAndCondition(
            String externalId,
            CardLanguage language,
            CardCondition condition
    );
}