package com.vilicus.finance.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .build();

        user.onCreate();

        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void testUserPrePersist() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void testUserPreUpdate() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.onCreate();

        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime updatedAtBefore = user.getUpdatedAt();

        // Simulate some delay
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(updatedAtBefore) || user.getUpdatedAt().isEqual(updatedAtBefore));
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testEmailValidation() {
        User user = User.builder()
                .email("invalid-email")
                .passwordHash("hashedpassword")
                .build();

        user.onCreate();

        // Email is set, even if invalid according to validation
        assertEquals("invalid-email", user.getEmail());
    }
}
