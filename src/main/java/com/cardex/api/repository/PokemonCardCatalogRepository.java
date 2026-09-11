package com.cardex.api.repository;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonCardCatalogRepository
        extends JpaRepository<PokemonCardCatalogEntity, Long>,
        JpaSpecificationExecutor<PokemonCardCatalogEntity> {

    List<PokemonCardCatalogEntity>
    findByCollectionId(String collectionId);

    boolean existsByCollectionId(String collectionId);

    Optional<PokemonCardCatalogEntity> findByExternalId(
            String externalId
    );

    List<PokemonCardCatalogEntity> findAllByExternalIdIn(
            List<String> externalIds
    );

    Page<PokemonCardCatalogEntity>
    findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}
