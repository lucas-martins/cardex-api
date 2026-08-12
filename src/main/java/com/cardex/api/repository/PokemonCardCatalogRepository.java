package com.cardex.api.repository;

import com.cardex.api.entity.PokemonCardCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokemonCardCatalogRepository
        extends JpaRepository<PokemonCardCatalogEntity, Long> {

    List<PokemonCardCatalogEntity>
    findByCollectionId(String collectionId);

    boolean existsByCollectionId(String collectionId);
}