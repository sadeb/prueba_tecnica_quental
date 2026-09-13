package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.dto.LoginRequest;
import com.quental.rickmorty.auth.dto.LoginResponse;
import com.quental.rickmorty.auth.dto.RegisterRequest;
import com.quental.rickmorty.auth.dto.RegisterResponse;
import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.UnauthorizedException;
import com.quental.rickmorty.user.User;
import com.quental.rickmorty.user.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserJpaRepository users = mock(UserJpaRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final AuthService service = new AuthService(users, encoder, tokenService);

    @Test
    void shouldNormaliseUsernameAndStoreHashedPassword() {
        when(users.existsByUsername("rick")).thenReturn(false);
        when(encoder.encode("wubbalubba")).thenReturn("$2a$hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 7L);
            return user;
        });

        RegisterResponse response = service.register(request("  Rick ", "wubbalubba"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("rick");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("$2a$hash");
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("rick");
    }

    @Test
    void shouldRejectDuplicateUsernameWithConflict() {
        when(users.existsByUsername("rick")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request("rick", "wubbalubba"))).isInstanceOf(ConflictException.class);
        verify(users, never()).save(any());
    }

    @Test
    void shouldIssueTokenWhenPasswordMatches() {
        User user = User.create("rick", "$2a$hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        when(users.findByUsername("rick")).thenReturn(Optional.of(user));
        when(encoder.matches("wubbalubba", "$2a$hash")).thenReturn(true);
        Instant expiresAt = Instant.parse("2026-09-14T00:00:00Z");
        when(tokenService.issue(7L, "rick")).thenReturn(new IssuedToken("a.b.c", expiresAt));

        LoginResponse response = service.login(login("RICK", "wubbalubba"));

        assertThat(response.getToken()).isEqualTo("a.b.c");
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getUsername()).isEqualTo("rick");
    }

    @Test
    void shouldRejectWrongPasswordWithUnauthorized() {
        when(users.findByUsername("rick")).thenReturn(Optional.of(User.create("rick", "$2a$hash")));
        when(encoder.matches("nope", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(login("rick", "nope"))).isInstanceOf(UnauthorizedException.class);
        verify(tokenService, never()).issue(anyLong(), any());
    }

    @Test
    void shouldRejectUnknownUserWithTheSameUnauthorizedError() {
        when(users.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(login("nobody", "whatever")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Bad credentials");
    }

    private static RegisterRequest request(String username, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private static LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
