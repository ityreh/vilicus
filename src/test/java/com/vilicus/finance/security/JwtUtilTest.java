package com.vilicus.finance.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String secret = "your-super-secret-key-change-in-production-must-be-at-least-256-bits-long-for-hs256";
        long expiration = 900000;  // 15 minutes
        long refreshExpiration = 604800000;  // 7 days

        jwtUtil = new JwtUtil(secret, expiration, refreshExpiration);
        userDetails = new User("test@example.com", "password", new ArrayList<>());
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testExtractEmail() {
        String token = jwtUtil.generateToken(userDetails);
        String email = jwtUtil.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void testIsTokenValid() {
        String token = jwtUtil.generateToken(userDetails);

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void testIsTokenInvalid() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtUtil.isTokenValid(invalidToken));
    }

    @Test
    void testTokenExpiration() {
        // Create a token with very short expiration
        JwtUtil shortExpiryUtil = new JwtUtil(
                "your-super-secret-key-change-in-production-must-be-at-least-256-bits-long-for-hs256",
                1,  // 1 millisecond expiration
                604800000
        );

        String token = shortExpiryUtil.generateToken(userDetails);

        // Wait a bit to ensure token expires
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(shortExpiryUtil.isTokenExpired(token));
    }

    @Test
    void testExtractEmailFromRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        String email = jwtUtil.extractEmail(refreshToken);

        assertEquals("test@example.com", email);
    }

    @Test
    void testDifferentTokensForDifferentUsers() {
        UserDetails user2 = new User("another@example.com", "password", new ArrayList<>());

        String token1 = jwtUtil.generateToken(userDetails);
        String token2 = jwtUtil.generateToken(user2);

        assertNotEquals(token1, token2);
        assertEquals("test@example.com", jwtUtil.extractEmail(token1));
        assertEquals("another@example.com", jwtUtil.extractEmail(token2));
    }
}
