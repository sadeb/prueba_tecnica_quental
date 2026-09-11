package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.domain.AccessTokenEntity;
import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final AccessTokenRepository repository;
    private final AuthProperties properties;

    public TokenService(AccessTokenRepository repository, AuthProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public IssuedToken issue(UserEntity user) {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenTtl());
        repository.save(new AccessTokenEntity(UUID.randomUUID().toString(), hash(value), user, now, expiresAt));
        return new IssuedToken(value, expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedUser> authenticate(String token) {
        if (token == null || token.length() < 40) return Optional.empty();
        return repository.findByTokenHash(hash(token))
                .filter(value -> value.isActiveAt(Instant.now()))
                .map(value -> new AuthenticatedUser(value.getUser().getId(), value.getUser().getUsername(), value.getUser().getRole()));
    }

    @Transactional
    public void revoke(String token) {
        if (token == null) return;
        repository.findByTokenHash(hash(token)).ifPresent(value -> {
            if (value.getRevokedAt() == null) value.revoke(Instant.now());
            repository.save(value);
        });
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte part : digest) result.append(String.format("%02x", part & 0xff));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
