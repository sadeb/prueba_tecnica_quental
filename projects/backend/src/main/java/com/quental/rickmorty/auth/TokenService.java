package com.quental.rickmorty.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Self-issued JWT-compact tokens signed with HMAC-SHA256 (ADR-005, bonus B1): header.payload.signature,
 * Base64 URL without padding, javax.crypto only. Claims: sub (username), uid, iat, exp.
 * Verification: exactly 3 parts, decodable Base64, signature compared in constant time, alg HS256,
 * exp in the future. Any failure -> InvalidTokenException (401).
 */
@Service
public class TokenService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final AuthTokenProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public TokenService(AuthTokenProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, Clock.systemUTC());
    }

    TokenService(AuthTokenProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public IssuedToken issue(long userId, String username) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofHours(properties.getTtlHours()));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", username);
        payload.put("uid", userId);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        String header = encode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String body = encode(toJson(payload));
        String signingInput = header + "." + body;
        String signature = encode(sign(signingInput));
        return new IssuedToken(signingInput + "." + signature, expiresAt);
    }

    public TokenClaims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Empty token");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw new InvalidTokenException("Token must have three non-empty parts");
        }
        byte[] expected = sign(parts[0] + "." + parts[1]);
        byte[] actual = decode(parts[2]);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new InvalidTokenException("Invalid signature");
        }
        JsonNode header = readJson(decode(parts[0]));
        if (!"HS256".equals(header.path("alg").asText())) {
            throw new InvalidTokenException("Unsupported algorithm");
        }
        JsonNode payload = readJson(decode(parts[1]));
        long exp = requiredLong(payload, "exp");
        long iat = requiredLong(payload, "iat");
        long uid = requiredLong(payload, "uid");
        String sub = payload.path("sub").asText(null);
        if (sub == null || sub.isBlank()) {
            throw new InvalidTokenException("Missing subject");
        }
        if (exp <= Instant.now(clock).getEpochSecond()) {
            throw new InvalidTokenException("Token expired");
        }
        return new TokenClaims(uid, sub, Instant.ofEpochSecond(iat), Instant.ofEpochSecond(exp));
    }

    private byte[] sign(String input) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return mac.doFinal(input.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", ex);
        }
    }

    private static String encode(byte[] bytes) {
        return ENCODER.encodeToString(bytes);
    }

    private static byte[] decode(String part) {
        try {
            return DECODER.decode(part);
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Token is not valid Base64 URL", ex);
        }
    }

    private byte[] toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialise token payload", ex);
        }
    }

    private JsonNode readJson(byte[] bytes) {
        try {
            JsonNode node = objectMapper.readTree(bytes);
            if (node == null || !node.isObject()) {
                throw new InvalidTokenException("Token part is not a JSON object");
            }
            return node;
        } catch (java.io.IOException ex) {
            throw new InvalidTokenException("Token part is not valid JSON", ex);
        }
    }

    private static long requiredLong(JsonNode payload, String claim) {
        JsonNode node = payload.get(claim);
        if (node == null || !node.canConvertToLong()) {
            throw new InvalidTokenException("Missing or invalid claim " + claim);
        }
        return node.asLong();
    }
}
