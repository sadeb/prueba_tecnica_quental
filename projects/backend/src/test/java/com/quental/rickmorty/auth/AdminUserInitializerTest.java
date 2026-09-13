package com.quental.rickmorty.auth;

import com.quental.rickmorty.user.User;
import com.quental.rickmorty.user.UserJpaRepository;
import com.quental.rickmorty.user.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserInitializerTest {

    private final UserJpaRepository users = mock(UserJpaRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AdminUserInitializer initializer = new AdminUserInitializer(properties(" Admin ", "secret-1234"), users, encoder);

    @Test
    void shouldCreateTheAdministratorWithNormalisedUsernameWhenMissing() {
        when(users.findByUsername("admin")).thenReturn(Optional.empty());
        when(encoder.encode("secret-1234")).thenReturn("$2a$new");

        initializer.run(new DefaultApplicationArguments());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("admin");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$new");
        assertThat(saved.getValue().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldDoNothingWhenPasswordAndRoleAlreadyMatch() {
        when(users.findByUsername("admin")).thenReturn(Optional.of(User.create("admin", "$2a$old", UserRole.ADMIN)));
        when(encoder.matches("secret-1234", "$2a$old")).thenReturn(true);

        initializer.run(new DefaultApplicationArguments());

        verify(users, never()).save(any());
        verify(encoder, never()).encode(any());
    }

    @Test
    void shouldRehashWhenTheConfiguredPasswordChanged() {
        User admin = User.create("admin", "$2a$old", UserRole.ADMIN);
        when(users.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(encoder.matches("secret-1234", "$2a$old")).thenReturn(false);
        when(encoder.encode("secret-1234")).thenReturn("$2a$new");

        initializer.run(new DefaultApplicationArguments());

        verify(users).save(admin);
        assertThat(admin.getPasswordHash()).isEqualTo("$2a$new");
    }

    @Test
    void shouldPromoteAnExistingUserWithTheSameUsername() {
        User existing = User.create("admin", "$2a$old");
        when(users.findByUsername("admin")).thenReturn(Optional.of(existing));
        when(encoder.matches("secret-1234", "$2a$old")).thenReturn(true);

        initializer.run(new DefaultApplicationArguments());

        verify(users).save(existing);
        assertThat(existing.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(existing.getPasswordHash()).isEqualTo("$2a$old");
    }

    private static AdminUserProperties properties(String username, String password) {
        AdminUserProperties properties = new AdminUserProperties();
        properties.setUsername(username);
        properties.setPassword(password);
        return properties;
    }
}
