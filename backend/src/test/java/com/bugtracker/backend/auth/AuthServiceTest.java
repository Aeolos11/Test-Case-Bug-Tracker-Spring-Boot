package com.bugtracker.backend.auth;

import com.bugtracker.backend.auth.dto.*;
import com.bugtracker.backend.security.JwtService;
import com.bugtracker.backend.user.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "Test User",
                "test@example.com",
                "hashed-password"
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test@example.com",
                "Test123456"
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Test123456"))
                .thenReturn("hashed-password");

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(request);

        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
        assertEquals("tester", response.role());

        verify(passwordEncoder).encode("Test123456");

        verify(userRepository).saveAndFlush(argThat(savedUser ->
                savedUser.getPasswordHash().equals("hashed-password")
        ));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test@example.com",
                "Test123456"
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "Test123456"
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Test123456", "hashed-password"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.token());
        assertEquals("test@example.com", response.user().email());
        assertEquals("tester", response.user().role());

        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrong-password"
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectUnknownEmail() {
        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "Test123456"
        );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void shouldReturnCurrentUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser(userId);

        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
        assertEquals("tester", response.role());
    }

    @Test
    void shouldRejectUnknownCurrentUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.getCurrentUser(userId)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}