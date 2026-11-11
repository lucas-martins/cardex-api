package com.cardex.api.repository;

import com.cardex.api.entity.DeckEntity;
import com.cardex.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<DeckEntity, Long> {

    List<DeckEntity> findAllByUserOrderByUpdatedAtDesc(UserEntity user);

    Optional<DeckEntity> findByIdAndUser(Long id, UserEntity user);

    boolean existsByIdAndUser(Long id, UserEntity user);
}
