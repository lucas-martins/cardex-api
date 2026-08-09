package com.cardex.api.component;

import com.cardex.api.entity.CardEntity;
import com.cardex.api.entity.CardHistoryEntity;
import com.cardex.api.enumeration.CardHistoryAction;
import com.cardex.api.repository.CardHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardHistoryRecorder {

    private final CardHistoryRepository cardHistoryRepository;

    public void record(
            CardEntity card,
            CardHistoryAction action,
            String description
    ) {
        CardHistoryEntity history = new CardHistoryEntity();

        history.setUser(card.getUser());
        history.setCardId(card.getId());
        history.setExternalId(card.getExternalId());
        history.setCardName(card.getName());
        history.setAction(action);
        history.setDescription(description);

        cardHistoryRepository.save(history);
    }
}