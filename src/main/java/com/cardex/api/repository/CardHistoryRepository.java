package com.cardex.api.repository;

import com.cardex.api.entity.CardHistoryEntity;
import com.cardex.api.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardHistoryRepository
        extends JpaRepository<CardHistoryEntity, Long> {

    Page<CardHistoryEntity> findByUser(
            UserEntity user,
            Pageable pageable
    );
}