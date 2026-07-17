package com.cardex.api.repository;

import com.cardex.api.entity.WishlistCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistCardRepository
        extends JpaRepository<WishlistCardEntity, Long> {

    boolean existsByExternalId(String externalId);

    List<WishlistCardEntity> findAllByOrderByCreatedAtDesc();
}