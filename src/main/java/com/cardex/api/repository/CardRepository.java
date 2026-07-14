package com.cardex.api.repository;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.enumeration.CardCondition;
import com.cardex.api.enumeration.CardLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    Optional<CardEntity> findByExternalIdAndLanguageAndCondition(
            String externalId,
            CardLanguage language,
            CardCondition condition
    );
}