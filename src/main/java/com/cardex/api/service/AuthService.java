package com.cardex.api.service;

import com.cardex.api.dto.request.LoginRequest;
import com.cardex.api.dto.request.RegisterRequest;
import com.cardex.api.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getAuthenticatedUser();
}