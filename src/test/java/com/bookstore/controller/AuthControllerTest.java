package com.bookstore.controller;

import com.bookstore.domain.Role;
import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.dto.user.UserDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.GlobalExceptionHandler;
import com.bookstore.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserAndReturn201Created() throws Exception {
        RegisterRequest request = new RegisterRequest("john@example.com", "password123", "John Doe");
        UserDto userDto = new UserDto(UUID.randomUUID(), "john@example.com", "John Doe", Role.ROLE_USER);
        AuthResponse response = new AuthResponse("mock-jwt-token", userDto);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("mock-jwt-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")))
                .andExpect(jsonPath("$.user.fullName", is("John Doe")));
    }

    @Test
    void shouldReturn400WhenRegisterPayloadIsInvalid() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "short", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void shouldLoginAndReturn200Ok() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UserDto userDto = new UserDto(UUID.randomUUID(), "john@example.com", "John Doe", Role.ROLE_USER);
        AuthResponse response = new AuthResponse("mock-jwt-token", userDto);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mock-jwt-token")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")));
    }

    @Test
    void shouldReturn400WhenRegisterEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "Existing User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email is already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Email is already in use")));
    }

    @Test
    void shouldReturn401WhenLoginCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Bad credentials")));
    }

    @Test
    void shouldReturn401WhenLoginUsernameNotFound() throws Exception {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found with email: unknown@example.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("User not found with email: unknown@example.com")));
    }
}
