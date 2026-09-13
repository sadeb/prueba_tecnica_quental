package com.quental.rickmorty.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.TestData;
import com.quental.rickmorty.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-0123456789";
    private static final Instant NOW = Instant.parse("2026-09-13T12:00:00Z");

    private final ObjectMapper objectMapper = TestData.objectMapper();
    private final TokenService service = service(SECRET, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void shouldRoundTripClaims() {
        IssuedToken issued = service.issue(42L, "rick", UserRole.USER);

        TokenClaims claims = service.parse(issued.getToken());

        assertThat(issued.getToken().split("\\.")).hasSize(3);
        assertThat(issued.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(claims.getUserId()).isEqualTo(42L);
        assertThat(claims.getUsername()).isEqualTo("rick");
        assertThat(claims.getRole()).isEqualTo(UserRole.USER);
        assertThat(claims.getIssuedAt()).isEqualTo(NOW);
        assertThat(claims.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
    }

    @Test
    void shouldCarryTheAdminRole() {
        IssuedToken issued = service.issue(1L, "admin", UserRole.ADMIN);

        assertThat(service.parse(issued.getToken()).getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldRejectTamperedSignature() {
        String token = service.issue(1L, "rick", UserRole.USER).getToken();
        String tampered = token.substring(0, token.length() - 2) + "AA";

        assertThatThrownBy(() -> service.parse(tampered)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRejectTamperedPayload() {
        String token = service.issue(1L, "rick", UserRole.USER).getToken();
        String[] parts = token.split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"rick\",\"uid\":999,\"iat\":0,\"exp\":9999999999}".getBytes());

        assertThatThrownBy(() -> service.parse(parts[0] + "." + forgedPayload + "." + parts[2]))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void shouldRejectExpiredToken() {
        TokenService past = service(SECRET, Clock.fixed(NOW.minus(Duration.ofHours(2)), ZoneOffset.UTC));
        String token = past.issue(1L, "rick", UserRole.USER).getToken();

        assertThatThrownBy(() -> service.parse(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        String token = service("another-secret-another-secret-another-secret", Clock.fixed(NOW, ZoneOffset.UTC))
                .issue(1L, "rick", UserRole.USER).getToken();

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(InvalidTokenException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"only.two", "a..c", "", "%%%.%%%.%%%", "a.b.c.d"})
    void shouldRejectMalformedToken(String token) {
        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(InvalidTokenException.class);
    }

    private TokenService service(String secret, Clock clock) {
        AuthTokenProperties properties = new AuthTokenProperties();
        properties.setSecret(secret);
        properties.setTtlHours(1);
        return new TokenService(properties, objectMapper, clock);
    }
}
