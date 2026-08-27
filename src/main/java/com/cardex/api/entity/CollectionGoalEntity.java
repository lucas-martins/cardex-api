package com.cardex.api.entity;

import com.cardex.api.enumeration.CardLanguage;
import com.cardex.api.enumeration.CollectionGoalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "collection_goals")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CollectionGoalEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;

    @Column(
            nullable = false,
            length = 150
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "goal_type",
            nullable = false,
            length = 50
    )
    private CollectionGoalType type;

    @Column(name = "target_value")
    private Integer targetValue;

    @Column(
            name = "collection_id",
            length = 50
    )
    private String collectionId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "language",
            length = 30
    )
    private CardLanguage language;
}