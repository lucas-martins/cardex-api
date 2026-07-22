package com.cardex.api.service;

import com.cardex.api.entity.UserEntity;

public interface JwtService {

    String generateToken(UserEntity user);

    long getExpirationSeconds();
}