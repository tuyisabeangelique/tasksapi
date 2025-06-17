package com.example.tasksapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Set the JWT secret for testing
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecretKeyForJwtTokenGenerationAndValidationInTests");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000); // 24 hours
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");

        // When
        String token = jwtUtils.generateJwtToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void getUserNameFromJwtToken_ShouldReturnUsername() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void validateJwtToken_ShouldReturnTrue_ForValidToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_ForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_ForEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtUtils.validateJwtToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_ForNullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtUtils.validateJwtToken(nullToken);

        // Then
        assertThat(isValid).isFalse();
    }
} 