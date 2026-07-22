package com.cardex.api.service;

import com.cardex.api.entity.UserEntity;

public interface AuthenticatedUserService {

    UserEntity getAuthenticatedUser();
}