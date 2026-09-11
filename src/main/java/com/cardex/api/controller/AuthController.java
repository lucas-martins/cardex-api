package com.cardex.api.controller;

import com.cardex.api.dto.request.ChangePasswordRequest;
import com.cardex.api.dto.request.ForgotPasswordRequest;
import com.cardex.api.dto.request.LoginRequest;
import com.cardex.api.dto.request.RegisterRequest;
import com.cardex.api.dto.request.ResetPasswordRequest;
import com.cardex.api.dto.request.UpdateProfileRequest;
import com.cardex.api.dto.response.AuthResponse;
import com.cardex.api.dto.response.ForgotPasswordResponse;
import com.cardex.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getAuthenticatedUser() {
        return ResponseEntity.ok(
                authService.getAuthenticatedUser()
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                authService.updateProfile(request)
        );
    }
}