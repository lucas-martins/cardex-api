package com.cardex.api.entity;

import com.cardex.api.enumeration.CardHistoryAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardHistoryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "external_id", length = 50)
    private String externalId;

    @Column(name = "card_name", nullable = false, length = 150)
    private String cardName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CardHistoryAction action;

    @Column(length = 1000)
    private String description;
}