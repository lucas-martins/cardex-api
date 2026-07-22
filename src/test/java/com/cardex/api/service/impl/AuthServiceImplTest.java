package com.cardex.api.service.impl;

import com.cardex.api.dto.request.LoginRequest;
import com.cardex.api.dto.request.RegisterRequest;
import com.cardex.api.dto.response.AuthResponse;
import com.cardex.api.entity.UserEntity;
import com.cardex.api.enumeration.UserRole;
import com.cardex.api.exception.EmailAlreadyRegisteredException;
import com.cardex.api.exception.InvalidCredentialsException;
import com.cardex.api.repository.UserRepository;
import com.cardex.api.service.AuthenticatedUserService;
import com.cardex.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String NAME = "Lucas Martins";
    private static final String EMAIL = "lucas@example.com";
    private static final String PASSWORD = "12345678";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final long EXPIRATION_SECONDS = 7200L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setName(NAME);
        user.setEmail(EMAIL);
        user.setPassword(ENCODED_PASSWORD);
        user.setRole(UserRole.USER);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest(
                NAME,
                "  LUCAS@EXAMPLE.COM  ",
                PASSWORD
        );

        when(userRepository.existsByEmailIgnoreCase(EMAIL))
                .thenReturn(false);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(user);

        mockTokenGeneration();

        AuthResponse response = authService.register(request);

        assertEquals(EMAIL, response.email());

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldEncodePasswordWhenRegisteringUser() {
        RegisterRequest request = new RegisterRequest(
                NAME,
                EMAIL,
                PASSWORD
        );

        when(userRepository.existsByEmailIgnoreCase(EMAIL))
                .thenReturn(false);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(user);

        mockTokenGeneration();

        authService.register(request);

        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest(
                NAME,
                EMAIL,
                PASSWORD
        );

        when(userRepository.existsByEmailIgnoreCase(EMAIL))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldLoginUser() {
        LoginRequest request = new LoginRequest(
                "  LUCAS@EXAMPLE.COM  ",
                PASSWORD
        );

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        mockTokenGeneration();

        AuthResponse response = authService.login(request);

        assertEquals(ACCESS_TOKEN, response.accessToken());
    }

    @Test
    void shouldThrowExceptionWhenLoginEmailDoesNotExist() {
        LoginRequest request = new LoginRequest(
                EMAIL,
                PASSWORD
        );

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginPasswordIsInvalid() {
        LoginRequest request = new LoginRequest(
                EMAIL,
                PASSWORD
        );

        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    private void mockTokenGeneration() {
        when(jwtService.generateToken(user))
                .thenReturn(ACCESS_TOKEN);

        when(jwtService.getExpirationSeconds())
                .thenReturn(EXPIRATION_SECONDS);
    }

    @Test
    void shouldReturnAuthenticatedUser() {
        when(authenticatedUserService.getAuthenticatedUser())
                .thenReturn(user);

        AuthResponse response = authService.getAuthenticatedUser();

        assertEquals(EMAIL, response.email());
    }
}