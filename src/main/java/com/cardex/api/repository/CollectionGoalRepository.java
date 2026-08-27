package com.cardex.api.repository;

import com.cardex.api.entity.CollectionGoalEntity;
import com.cardex.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionGoalRepository
        extends JpaRepository<CollectionGoalEntity, Long> {

    List<CollectionGoalEntity>
    findAllByUserOrderByCreatedAtDesc(
            UserEntity user
    );

    Optional<CollectionGoalEntity>
    findByIdAndUser(
            Long id,
            UserEntity user
    );

    long countByUser(
            UserEntity user
    );
}