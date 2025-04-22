package com.lenin.hotel.controller.authentication;

import com.lenin.hotel.authentication.controller.AuthController;
import com.lenin.hotel.authentication.dto.request.SignupRequest;
import com.lenin.hotel.authentication.dto.request.TokenRefreshRequest;
import com.lenin.hotel.authentication.dto.response.TokenRefreshResponse;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.security.JwtUtil;
import com.lenin.hotel.authentication.security.RefreshTokenService;
import com.lenin.hotel.authentication.security.UserDetailsImpl;
import com.lenin.hotel.authentication.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private JwtUtil jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserServiceImpl userServiceImpl;

    @Mock
    private UserDetailsService userDetailsService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testRegisterUser() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");

        Map<String, String> expectedResponse = Map.of("message", "User registered successfully!");
        when(userServiceImpl.signup(any(SignupRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> responseEntity = authController.registerUser(signupRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    void testActiveAccount() {
        // Arrange
        String token = "testToken";
        Map<String, String> expectedResponse = Map.of("message", "Account activated successfully");
        when(userServiceImpl.activeAccount(any(String.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> responseEntity = authController.activeAccount(token);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    void testRefreshToken_ValidToken() {
        // Arrange
        TokenRefreshRequest request = new TokenRefreshRequest();
        String refreshToken = "validRefreshToken";
        request.setRefreshToken(refreshToken);

        RefreshToken mockRefreshToken = new RefreshToken();
        com.lenin.hotel.authentication.model.User user = new com.lenin.hotel.authentication.model.User();
        user.setUsername("testuser");
        mockRefreshToken.setUser(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", null, Collections.emptyList());

        when(refreshTokenService.findByToken(refreshToken)).thenReturn(Optional.of(mockRefreshToken));
        when(refreshTokenService.verifyExpiration(mockRefreshToken)).thenReturn(mockRefreshToken);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("newToken");

        // Act
        ResponseEntity<?> responseEntity = authController.refreshtoken(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        TokenRefreshResponse responseBody = (TokenRefreshResponse) responseEntity.getBody();
        assertEquals("newToken", responseBody.getAccessToken());
        assertEquals(refreshToken, responseBody.getRefreshToken());
    }

    @Test
    void testForgotPassword_ValidEmail() {
        // Arrange
        Map<String, String> request = Map.of("email", "test@example.com");
        Map<String, String> expectedResponse = Map.of("message", "Password reset email sent");
        when(userServiceImpl.forgotPassword("test@example.com")).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> responseEntity = authController.forgotPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }
}