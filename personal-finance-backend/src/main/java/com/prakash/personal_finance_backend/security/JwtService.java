package com.prakash.personal_finance_backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hand-rolled HS256 JWT issue/validate. No jjwt/nimbus dependency: this is
 * only HMAC-SHA256 signing over a JSON header+payload, which the JDK
 * (javax.crypto.Mac) already does — pulling in a whole JWT library for that
 * was not worth another external-dependency resolution risk.
 *
 * Token shape is a standard 3-part JWT (header.payload.signature, all
 * base64url, no padding) so it still decodes fine in jwt.io or any other
 * tool if needed for debugging.
 */
@Component
public class JwtService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, Object> HEADER = Map.of("alg", "HS256", "typ", "JWT");

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must decode to at least 256 bits (32 bytes) — generate one with "
                            + "`openssl rand -base64 32`");
        }
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.expirationMillis = Duration.ofMinutes(expirationMinutes).toMillis();
    }

    public IssuedToken issueToken(Long userId, String email) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMillis);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", String.valueOf(userId));
        claims.put("email", email);
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());

        String signingInput = encodeSegment(HEADER) + "." + encodeSegment(claims);
        String token = signingInput + "." + sign(signingInput);
        return new IssuedToken(token, issuedAt, expiresAt);
    }

    public JwtClaims validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtValidationException("Malformed token");
        }

        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = sign(signingInput);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new JwtValidationException("Invalid signature");
        }

        Map<String, Object> claims = decodeSegment(parts[1]);
        long exp = ((Number) claims.get("exp")).longValue();
        if (Instant.now().getEpochSecond() > exp) {
            throw new JwtValidationException("Token expired");
        }

        try {
            Long userId = Long.valueOf((String) claims.get("sub"));
            String email = (String) claims.get("email");
            return new JwtClaims(userId, email);
        } catch (NumberFormatException | ClassCastException | NullPointerException e) {
            throw new JwtValidationException("Malformed token claims");
        }
    }

    private String encodeSegment(Map<String, Object> map) {
        try {
            byte[] json = JSON.writeValueAsBytes(map);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to encode JWT segment", e);
        }
    }

    private Map<String, Object> decodeSegment(String segment) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(segment);
            return JSON.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JacksonException | IllegalArgumentException e) {
            throw new JwtValidationException("Malformed token payload");
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signatureBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    public record IssuedToken(String token, Instant issuedAt, Instant expiresAt) {
    }
}