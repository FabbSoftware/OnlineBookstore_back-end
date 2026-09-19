package com.bookstore.service;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.dto.auth.AuthResponse;
import com.bookstore.dto.auth.LoginRequest;
import com.bookstore.dto.auth.RegisterRequest;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.UserMapper;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.email())) {
                throw new BadRequestException("Email is already in use");
            }

            CharBuffer passwordBuffer = request.password() != null
                    ? CharBuffer.wrap(request.password())
                    : CharBuffer.wrap("");

            User user = new User(
                    request.email(),
                    passwordEncoder.encode(passwordBuffer),
                    request.fullName(),
                    Role.ROLE_USER
            );

            User savedUser = userRepository.save(user);
            String token = jwtService.generateToken(savedUser);

            return new AuthResponse(token, userMapper.toDto(savedUser));
        } finally {
            request.erasePassword();
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

            String token = jwtService.generateToken(user);
            return new AuthResponse(token, userMapper.toDto(user));
        } finally {
            request.erasePassword();
        }
    }
}
