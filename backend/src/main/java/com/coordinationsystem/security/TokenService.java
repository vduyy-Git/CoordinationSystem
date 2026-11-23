package com.coordinationsystem.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final long TOKEN_LIFETIME_SECONDS = 3600;
    private final byte[] secret;

    public TokenService(@Value("${app.security.token-secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("APP_TOKEN_SECRET phải có ít nhất 32 byte");
        }
    }

    public String create(String email) {
        long expires = Instant.now().getEpochSecond() + TOKEN_LIFETIME_SECONDS;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((email + "\n" + expires).getBytes(StandardCharsets.UTF_8));
        return payload + "." + sign(payload);
    }

    public String validateAndGetEmail(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !MessageDigest.isEqual(
                    parts[1].getBytes(StandardCharsets.US_ASCII), sign(parts[0]).getBytes(StandardCharsets.US_ASCII))) {
                return null;
            }
            String[] claims = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8).split("\\n", -1);
            if (claims.length != 2 || Long.parseLong(claims[1]) <= Instant.now().getEpochSecond()) return null;
            return claims[0];
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public long expiresInSeconds() { return TOKEN_LIFETIME_SECONDS; }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký access token", ex);
        }
    }
}
