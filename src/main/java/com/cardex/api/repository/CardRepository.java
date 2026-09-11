package com.cardex.api.repository;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.entity.UserEntity;
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

    Optional<CardEntity> findByUserAndExternalIdAndLanguageAndCondition(
            UserEntity user,
            String externalId,
            CardLanguage language,
            CardCondition condition
    );

    @Query("""
        select coalesce(sum(card.quantity), 0)
        from CardEntity card
        where card.user = :user
        """)
    Long sumTotalQuantity(UserEntity user);

    @Query("""
        select count(distinct card.language)
        from CardEntity card
        where card.user = :user
        """)
    long countDifferentLanguages(UserEntity user);

    @Query("""
        select count(distinct card.collectionName)
        from CardEntity card
        where card.user = :user
        and card.collectionName is not null
        """)
    long countDifferentCollections(UserEntity user);

    Optional<CardEntity> findFirstByUserOrderByQuantityDescCreatedAtDesc(
            UserEntity user
    );

    @Query("""
    select
        card.collectionName as name,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.user = :user
      and card.collectionName is not null
    group by card.collectionName
    order by sum(card.quantity) desc, card.collectionName asc
    """)
    List<CollectionQuantityProjection> findQuantityGroupedByCollection(
            UserEntity user
    );

    @Query("""
    select
        card.language as language,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.user = :user
      and card.language is not null
    group by card.language
    order by sum(card.quantity) desc, card.language asc
    """)
    List<LanguageQuantityProjection> findQuantityGroupedByLanguage(
            UserEntity user
    );

    @Query("""
    select
        card.condition as condition,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.user = :user
      and card.condition is not null
    group by card.condition
    order by sum(card.quantity) desc, card.condition asc
    """)
    List<ConditionQuantityProjection> findQuantityGroupedByCondition(
            UserEntity user
    );

    @Query("""
    select
        card.rarity as rarity,
        sum(card.quantity) as quantity
    from CardEntity card
    where card.user = :user
      and card.rarity is not null
      and trim(card.rarity) <> ''
    group by card.rarity
    order by sum(card.quantity) desc, card.rarity asc
    """)
    List<RarityQuantityProjection> findQuantityGroupedByRarity(
            UserEntity user
    );

    boolean existsByUserAndFavoriteTrue(UserEntity user);

    @Query("""
        select count(card)
        from CardEntity card
        where card.user = :user
        and card.rarity is not null
        and (
            lower(card.rarity) like '%rare%'
            or lower(card.rarity) like '%secret%'
            or lower(card.rarity) like '%ultra%'
            or lower(card.rarity) like '%illustration%'
        )
        """)
    long countRareCards(UserEntity user);

    @Query("""
        select
            card.collectionId as collectionId,
            card.collectionName as collectionName,
            card.collectionTotal as collectionTotal,
            count(distinct card.externalId) as ownedCards
        from CardEntity card
        where card.user = :user
          and card.collectionId is not null
          and card.collectionTotal is not null
        group by
            card.collectionId,
            card.collectionName,
            card.collectionTotal
        order by count(distinct card.externalId) desc,
                 card.collectionName asc
        """)
    List<CollectionProgressProjection> findCollectionProgress(
            UserEntity user
    );

    List<CardEntity> findByUserAndCollectionId(
            UserEntity user,
            String collectionId
    );

    Optional<CardEntity> findByIdAndUser(
            Long id,
            UserEntity user
    );

    boolean existsByIdAndUser(
            Long id,
            UserEntity user
    );

    List<CardEntity> findByUserAndExternalIdIn(
            UserEntity user,
            List<String> externalIds
    );

    @Query("""
    select coalesce(sum(card.quantity), 0)
    from CardEntity card
    where card.user = :user
      and card.language = :language
    """)
    Long sumTotalQuantityByUserAndLanguage(
            UserEntity user,
            CardLanguage language
    );

    @Query("""
    select count(distinct card.externalId)
    from CardEntity card
    where card.user = :user
      and card.collectionId = :collectionId
    """)
    long countDistinctCardsByUserAndCollectionId(
            UserEntity user,
            String collectionId
    );

    @Query("""
    select
        card.collectionId as collectionId,
        count(distinct card.externalId) as ownedCards
    from CardEntity card
    where card.user = :user
      and card.collectionId is not null
    group by card.collectionId
    """)
    List<CollectionOwnedCardsProjection>
    findOwnedCardsGroupedByCollection(
            UserEntity user
    );

    @Query("""
    select
        coalesce(sum(case
            when catalog.marketPriceUsd is not null
            then card.quantity * catalog.marketPriceUsd
            else 0
        end), 0) as estimatedValueUsd,
        coalesce(sum(case
            when catalog.marketPriceEur is not null
            then card.quantity * catalog.marketPriceEur
            else 0
        end), 0) as estimatedValueEur,
        coalesce(sum(case
            when catalog.marketPriceUsd is not null
              or catalog.marketPriceEur is not null
            then card.quantity
            else 0
        end), 0) as pricedCopies,
        coalesce(sum(case
            when catalog.marketPriceUsd is null
             and catalog.marketPriceEur is null
            then card.quantity
            else 0
        end), 0) as unpricedCopies
    from CardEntity card
    left join PokemonCardCatalogEntity catalog
        on catalog.externalId = card.externalId
    where card.user = :user
    """)
    CollectionValueProjection sumCollectionValue(UserEntity user);

    @Query("""
    select
        card.collectionId as collectionId,
        card.collectionName as collectionName,
        coalesce(sum(case
            when catalog.marketPriceUsd is not null
            then card.quantity * catalog.marketPriceUsd
            else 0
        end), 0) as estimatedValueUsd,
        coalesce(sum(case
            when catalog.marketPriceEur is not null
            then card.quantity * catalog.marketPriceEur
            else 0
        end), 0) as estimatedValueEur
    from CardEntity card
    left join PokemonCardCatalogEntity catalog
        on catalog.externalId = card.externalId
    where card.user = :user
      and card.collectionId is not null
    group by
        card.collectionId,
        card.collectionName
    order by coalesce(sum(case
            when catalog.marketPriceUsd is not null
            then card.quantity * catalog.marketPriceUsd
            else 0
        end), 0) desc,
        card.collectionName asc
    """)
    List<CollectionValueByCollectionProjection>
    findEstimatedValueGroupedByCollection(UserEntity user);
}