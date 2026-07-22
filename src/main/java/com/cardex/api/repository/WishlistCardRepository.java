package com.cardex.api.repository;

import com.cardex.api.entity.UserEntity;
import com.cardex.api.entity.WishlistCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistCardRepository
        extends JpaRepository<WishlistCardEntity, Long> {

    boolean existsByUserAndExternalId(
            UserEntity user,
            String externalId
    );

    List<WishlistCardEntity> findAllByUserOrderByCreatedAtDesc(
            UserEntity user
    );

    Optional<WishlistCardEntity> findByIdAndUser(
            Long id,
            UserEntity user
    );
}