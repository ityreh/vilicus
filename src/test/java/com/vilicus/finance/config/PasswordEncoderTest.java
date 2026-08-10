package com.vilicus.finance.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void testPasswordEncoding() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "mySecurePassword123!";

        String encodedPassword = encoder.encode(password);

        assertNotNull(encodedPassword);
        assertNotEquals(password, encodedPassword);
    }

    @Test
    void testPasswordMatches() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "mySecurePassword123!";

        String encodedPassword = encoder.encode(password);

        assertTrue(encoder.matches(password, encodedPassword));
    }

    @Test
    void testPasswordMismatch() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "mySecurePassword123!";
        String differentPassword = "differentPassword456!";

        String encodedPassword = encoder.encode(password);

        assertFalse(encoder.matches(differentPassword, encodedPassword));
    }

    @Test
    void testDifferentEncodingsForSamePassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "mySecurePassword123!";

        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        assertNotEquals(encoded1, encoded2);
        assertTrue(encoder.matches(password, encoded1));
        assertTrue(encoder.matches(password, encoded2));
    }
}
