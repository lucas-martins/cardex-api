package com.cardex.api.service;

import com.cardex.api.dto.request.ChangePasswordRequest;
import com.cardex.api.dto.request.ForgotPasswordRequest;
import com.cardex.api.dto.request.LoginRequest;
import com.cardex.api.dto.request.RegisterRequest;
import com.cardex.api.dto.request.ResetPasswordRequest;
import com.cardex.api.dto.request.UpdateProfileRequest;
import com.cardex.api.dto.response.AuthResponse;
import com.cardex.api.dto.response.ForgotPasswordResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    AuthResponse getAuthenticatedUser();

    void changePassword(ChangePasswordRequest request);

    AuthResponse updateProfile(UpdateProfileRequest request);
}