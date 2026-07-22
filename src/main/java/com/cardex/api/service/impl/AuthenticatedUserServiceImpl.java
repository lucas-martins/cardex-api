package com.cardex.api.service.impl;

import com.cardex.api.entity.UserEntity;
import com.cardex.api.exception.UserNotFoundException;
import com.cardex.api.repository.UserRepository;
import com.cardex.api.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl
        implements AuthenticatedUserService {

    private static final String USER_ID_CLAIM = "userId";

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException(
                    "Authenticated JWT user was not found."
            );
        }

        Number userIdClaim = jwtAuthentication
                .getToken()
                .getClaim(USER_ID_CLAIM);

        if (userIdClaim == null) {
            throw new IllegalStateException(
                    "JWT does not contain the userId claim."
            );
        }

        Long userId = userIdClaim.longValue();

        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}