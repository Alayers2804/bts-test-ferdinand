
package com.bts.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

    private final SecretKey signingKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    public TokenService(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String getUsernameFromAccessToken(String token) {
        var claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        String tokenType = claims.get("token_type", String.class);

        if (!"access".equals(tokenType)) {
            throw new IllegalArgumentException(
                "An access token is required"
            );
        }

        return claims.getSubject();
    }


    public String generateAccessToken(String username) {
        return generateToken(
            username,
            "access",
            ACCESS_TOKEN_EXPIRATION
        );
    }

    public String generateRefreshToken(String username) {
        return generateToken(
            username,
            "refresh",
            REFRESH_TOKEN_EXPIRATION
        );
    }

    private String generateToken(
            String username,
            String tokenType,
            long expirationMillis) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
            .subject(username)
            .claim("token_type", tokenType)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact();
    }
}
