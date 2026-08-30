package com.cardex.api.repository;

import com.cardex.api.entity.PokemonSetCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonSetCatalogRepository
        extends JpaRepository<PokemonSetCatalogEntity, Long> {

    List<PokemonSetCatalogEntity>
    findAllByOrderByNameAsc();

    Optional<PokemonSetCatalogEntity>
    findByCollectionId(
            String collectionId
    );
}