package com.cardex.api.service.impl;

import com.cardex.api.dto.response.CardHistoryResponse;
import com.cardex.api.entity.CardHistoryEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.enumeration.CardHistoryAction;
import com.cardex.api.repository.CardHistoryRepository;
import com.cardex.api.repository.CardRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.CardHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardHistoryServiceImpl
        implements CardHistoryService {

    private final CardHistoryRepository cardHistoryRepository;
    private final CardRepository cardRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional(readOnly = true)
    public Page<CardHistoryResponse> findAll(
            int page,
            int size,
            CardHistoryAction action,
            String cardName
    ) {
        UserEntity authenticatedUser =
                authenticatedUserService.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        String normalizedCardName =
                cardName != null
                        ? cardName.trim()
                        : null;

        boolean hasCardName =
                normalizedCardName != null
                        && !normalizedCardName.isBlank();

        Page<CardHistoryEntity> historyPage;

        if (action == null && !hasCardName) {
            historyPage =
                    cardHistoryRepository.findByUser(
                            authenticatedUser,
                            pageable
                    );
        } else if (action != null && !hasCardName) {
            historyPage =
                    cardHistoryRepository.findByUserAndAction(
                            authenticatedUser,
                            action,
                            pageable
                    );
        } else if (action == null) {
            historyPage =
                    cardHistoryRepository
                            .findByUserAndCardNameContainingIgnoreCase(
                                    authenticatedUser,
                                    normalizedCardName,
                                    pageable
                            );
        } else {
            historyPage =
                    cardHistoryRepository
                            .findByUserAndActionAndCardNameContainingIgnoreCase(
                                    authenticatedUser,
                                    action,
                                    normalizedCardName,
                                    pageable
                            );
        }

        return historyPage.map(history -> {

            boolean cardExists =
                    history.getCardId() != null
                            && cardRepository.existsByIdAndUser(
                            history.getCardId(),
                            authenticatedUser
                    );

            return new CardHistoryResponse(
                    history.getId(),
                    history.getCardId(),
                    history.getExternalId(),
                    history.getCardName(),
                    history.getAction(),
                    history.getDescription(),
                    history.getCreatedAt(),
                    cardExists
            );
        });
    }
}