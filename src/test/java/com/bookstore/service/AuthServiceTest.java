package com.bookstore.service;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.dto.user.UserDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.mapper.UserMapper;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                userMapper
        );
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("john@example.com", "secret123", "John Doe");
        User savedUser = new User(userId, "john@example.com", "encodedPassword", "John Doe", Role.ROLE_USER);
        UserDto userDto = new UserDto(userId, "john@example.com", "John Doe", Role.ROLE_USER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");
        when(userMapper.toDto(savedUser)).thenReturn(userDto);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.user()).isEqualTo(userDto);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "secret123", "Existing User");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        UUID userId = UUID.randomUUID();
        LoginRequest request = new LoginRequest("john@example.com", "secret123");
        User user = new User(userId, "john@example.com", "encodedPassword", "John Doe", Role.ROLE_USER);
        UserDto userDto = new UserDto(userId, "john@example.com", "John Doe", Role.ROLE_USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-login-token");
        when(userMapper.toDto(user)).thenReturn(userDto);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-login-token");
        assertThat(response.user().email()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowExceptionWhenLoginFailsWithInvalidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
