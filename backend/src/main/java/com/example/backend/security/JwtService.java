package com.example.backend.security;

import com.example.backend.dto.response.jwt.JwtClaims;
import com.example.backend.share.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    public static final String ACCESS_TOKEN_TYPE = "ACCESS";
    public static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(CustomUserPrincipal principal) {
        return generateToken(principal, ACCESS_TOKEN_TYPE, accessTokenExpiration);
    }

    public String generateRefreshToken(CustomUserPrincipal principal) {
        return generateToken(principal, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
    }

    private String generateToken(CustomUserPrincipal principal, String tokenType, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", principal.getUserId());
        claims.put("email", principal.getEmail());
        claims.put("role", principal.getRole().name());
        claims.put("type", tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(principal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object value = extractAllClaims(token).get("userId");
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        return Long.valueOf(String.valueOf(value));
    }

    public UserRole extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return UserRole.valueOf(role);
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(extractType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(extractType(token));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public JwtClaims extractJwtClaims(String token) {
        return new JwtClaims(
                extractUserId(token),
                extractEmail(token),
                extractRole(token),
                extractType(token)
        );
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
