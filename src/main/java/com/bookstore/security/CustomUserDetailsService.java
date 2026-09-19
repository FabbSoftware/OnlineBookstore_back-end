package com.bookstore.security;

import com.bookstore.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByEmail(username)
                .map(UserPrincipal::fromUserForAuthentication)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public UserDetails loadUserByUsernameWithoutCredentials(String username) throws UsernameNotFoundException {
        return userService.findByEmail(username)
                .map(UserPrincipal::fromUserForJwt)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
