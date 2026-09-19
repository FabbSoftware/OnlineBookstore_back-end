package com.bookstore.service;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User(sampleUserId, "john@example.com", "encodedPassword", "John Doe", Role.ROLE_USER);
    }

    @Test
    void findByEmailShouldReturnUserWhenFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByEmail("john@example.com");

        assertThat(result).isPresent().contains(sampleUser);
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void findByEmailShouldReturnEmptyWhenNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmail("notfound@example.com");
    }

    @Test
    void getByEmailShouldReturnUserWhenFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        User result = userService.getByEmail("john@example.com");

        assertThat(result).isEqualTo(sampleUser);
    }

    @Test
    void getByEmailShouldThrowResourceNotFoundExceptionWhenNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("notfound@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: notfound@example.com");
    }

    @Test
    void existsByEmailShouldReturnTrueWhenUserExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("john@example.com");

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void existsByEmailShouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("notfound@example.com");

        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("notfound@example.com");
    }

    @Test
    void findByIdShouldReturnUserWhenFound() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findById(sampleUserId);

        assertThat(result).isPresent().contains(sampleUser);
        verify(userRepository).findById(sampleUserId);
    }

    @Test
    void getByIdShouldReturnUserWhenFound() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        User result = userService.getById(sampleUserId);

        assertThat(result).isEqualTo(sampleUser);
    }

    @Test
    void getByIdShouldThrowResourceNotFoundExceptionWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: " + missingId);
    }

    @Test
    void saveShouldPersistAndReturnUser() {
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        User result = userService.save(sampleUser);

        assertThat(result).isEqualTo(sampleUser);
        verify(userRepository).save(sampleUser);
    }
}
