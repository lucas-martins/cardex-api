package com.cardex.api.repository;

import com.cardex.api.entity.DeckCardEntity;
import com.cardex.api.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeckCardRepository
        extends JpaRepository<DeckCardEntity, Long> {

    Optional<DeckCardEntity> findByIdAndDeck(Long id, DeckEntity deck);

    Optional<DeckCardEntity> findByDeckAndExternalId(
            DeckEntity deck,
            String externalId
    );
}
