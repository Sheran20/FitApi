package com.sgt.fitapi.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class JwtService {

    private final byte[] secretBytes;
    private final long expirationMs;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs,
            ObjectMapper objectMapper
    ) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.objectMapper = objectMapper;
    }

    public String generateToken(UserDetails userDetails) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + expirationMs;

        Map<String, Object> payload = Map.of(
                "sub", userDetails.getUsername(),   // email in our case
                "iat", nowMillis / 1000,
                "exp", expMillis / 1000
        );

        try {
            String headerJson = objectMapper.writeValueAsString(
                    Map.of("alg", "HS256", "typ", "JWT")
            );
            String payloadJson = objectMapper.writeValueAsString(payload);

            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            String unsignedToken = headerB64 + "." + payloadB64;
            String signatureB64 = base64UrlEncode(sign(unsignedToken.getBytes(StandardCharsets.UTF_8)));

            return unsignedToken + "." + signatureB64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public String extractUsername(String token) {
        Map<String, Object> claims = parsePayload(token);
        Object sub = claims.get("sub");
        return sub != null ? sub.toString() : null;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Map<String, Object> claims = parsePayload(token);
            String username = (String) claims.get("sub");
            if (username == null || !username.equals(userDetails.getUsername())) {
                return false;
            }

            Object expObj = claims.get("exp");
            if (expObj instanceof Number expNum) {
                long expSeconds = expNum.longValue();
                long nowSeconds = Instant.now().getEpochSecond();
                if (expSeconds < nowSeconds) {
                    return false; // expired
                }
            }

            // signature already checked in parsePayload
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== internal helpers =====

    private Map<String, Object> parsePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            byte[] expectedSig = sign(unsignedToken.getBytes(StandardCharsets.UTF_8));
            byte[] actualSig = base64UrlDecode(parts[2]);

            if (!constantTimeEquals(expectedSig, actualSig)) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            byte[] payloadBytes = base64UrlDecode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }

    private byte[] sign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
