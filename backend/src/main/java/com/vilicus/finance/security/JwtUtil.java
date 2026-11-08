package com.vilicus.finance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@SuppressWarnings("deprecation")

@Component
public class JwtUtil {

    private final String jwtSecret;
    private final long jwtExpiration;
    private final long refreshExpiration;
    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret,
                   @Value("${jwt.expiration}") long jwtExpiration,
                   @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate access token (15 minutes expiration)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate refresh token (7 days expiration)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), refreshExpiration);
    }

    /**
     * Internal method to generate JWT tokens
     */
    private String generateToken(String email, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String extractEmail(String token) {
        return getTokenBody(token).getSubject();
    }

    /**
     * Validate JWT token
     */
    public boolean isTokenValid(String token) {
        try {
            getTokenBody(token);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract claims from token
     */
    private Claims getTokenBody(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getTokenBody(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
