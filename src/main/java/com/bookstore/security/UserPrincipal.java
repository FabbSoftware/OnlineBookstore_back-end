package com.bookstore.security;

import com.bookstore.domain.Role;
import com.bookstore.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserPrincipal implements UserDetails, CredentialsContainer {

    private final UUID id;
    private final String email;
    private final String fullName;
    private final Role role;
    private char[] password;

    public static UserPrincipal fromUserForJwt(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), null);
    }

    public static UserPrincipal fromUserForAuthentication(User user) {
        char[] pwd = user.getPassword() != null ? user.getPassword().toCharArray() : null;
        return new UserPrincipal(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), pwd);
    }

    public UserPrincipal(UUID id, String email, String fullName, Role role, char[] password) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role != null ? role : Role.ROLE_USER;
        this.password = password;
    }

    @Override
    public void eraseCredentials() {
        if (password != null) {
            Arrays.fill(password, '\0');
            password = null;
        }
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password != null ? new String(password) : null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
