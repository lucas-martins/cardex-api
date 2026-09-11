package com.cardex.api.service.impl;

import com.cardex.api.dto.response.CollectionChecklistResponse;
import com.cardex.api.dto.response.CollectionProgressResponse;
import com.cardex.api.dto.response.PublicShareCollectionResponse;
import com.cardex.api.dto.response.PublicShareSummaryResponse;
import com.cardex.api.dto.response.ShareStatusResponse;
import com.cardex.api.entity.PokemonSetCatalogEntity;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.exception.ShareNotFoundException;
import com.cardex.api.repository.UserRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.CardService;
import com.cardex.api.service.PokemonSetCatalogService;
import com.cardex.api.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final CardService cardService;
    private final PokemonSetCatalogService pokemonSetCatalogService;

    @Override
    @Transactional(readOnly = true)
    public ShareStatusResponse getStatus() {
        return toStatus(authenticatedUserService.getAuthenticatedUser());
    }

    @Override
    @Transactional
    public ShareStatusResponse enable() {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();

        if (user.getShareToken() == null || user.getShareToken().isBlank()) {
            user.setShareToken(
                    UUID.randomUUID().toString().replace("-", "")
            );
        }

        user.setShareEnabled(true);

        return toStatus(user);
    }

    @Override
    @Transactional
    public void disable() {
        UserEntity user = authenticatedUserService.getAuthenticatedUser();
        user.setShareEnabled(false);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicShareSummaryResponse getPublicSummary(String token) {
        UserEntity owner = findSharedOwner(token);

        List<CollectionProgressResponse> progress =
                cardService.getCollectionProgressForUser(owner);

        Map<String, PokemonSetCatalogEntity> setsById =
                pokemonSetCatalogService.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                PokemonSetCatalogEntity::getCollectionId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        List<PublicShareCollectionResponse> collections =
                progress.stream()
                        .map(item -> {
                            PokemonSetCatalogEntity set =
                                    setsById.get(item.collectionId());

                            return new PublicShareCollectionResponse(
                                    item.collectionId(),
                                    item.collectionName(),
                                    set != null ? set.getSeries() : null,
                                    set != null ? set.getPrintedTotal() : null,
                                    set != null
                                            ? set.getTotal()
                                            : (int) item.totalCards(),
                                    item.ownedCards(),
                                    item.completionPercentage()
                            );
                        })
                        .toList();

        return new PublicShareSummaryResponse(
                owner.getName(),
                owner.getShareToken(),
                collections
        );
    }

    @Override
    @Transactional
    public CollectionChecklistResponse getPublicChecklist(
            String token,
            String collectionId
    ) {
        UserEntity owner = findSharedOwner(token);
        return cardService.getCollectionChecklistForUser(owner, collectionId);
    }

    private UserEntity findSharedOwner(String token) {
        return userRepository
                .findByShareTokenAndShareEnabledTrue(token)
                .orElseThrow(ShareNotFoundException::new);
    }

    private ShareStatusResponse toStatus(UserEntity user) {
        String token = user.isShareEnabled() ? user.getShareToken() : null;
        String shareUrl = token != null ? "/share/" + token : null;

        return new ShareStatusResponse(
                user.isShareEnabled(),
                token,
                shareUrl
        );
    }
}
