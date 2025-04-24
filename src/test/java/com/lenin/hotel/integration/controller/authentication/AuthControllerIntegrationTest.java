package com.lenin.hotel.integration.controller.authentication;

import com.lenin.hotel.authentication.dto.request.LoginRequest;
import com.lenin.hotel.authentication.dto.request.SignupRequest;
import com.lenin.hotel.authentication.dto.request.TokenRefreshRequest;
import com.lenin.hotel.authentication.dto.response.TokenRefreshResponse;
import com.lenin.hotel.authentication.model.RefreshToken;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RefreshTokenRepository;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class AuthControllerIntegrationTest extends TestDynamicProperties {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String testUsername;
    private String testEmail;
    private String testPassword;
    private String refreshTokenValue;
    private Role userRole;

    @BeforeEach
    public void setup() {
        // Create test data with unique identifiers
        testUsername = "testuser" + UUID.randomUUID().toString().substring(0, 8);
        testEmail = "test" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        testPassword = "Test123!";

        // Clean up database tables in correct order to respect foreign key constraints
        try {
            jdbcTemplate.execute("DELETE FROM refresh_token");
            jdbcTemplate.execute("DELETE FROM favorites");
            jdbcTemplate.execute("DELETE FROM bookings"); // Add this line
            jdbcTemplate.execute("DELETE FROM hotel_amenity");
            jdbcTemplate.execute("DELETE FROM hotels WHERE owner_id != 1");
            jdbcTemplate.execute("DELETE FROM user_role WHERE user_id != 1");
            jdbcTemplate.execute("DELETE FROM users WHERE id != 1 AND username != 'admin'");
        } catch (Exception e) {
            System.err.println("Database cleanup error: " + e.getMessage());
        }

        // Ensure ROLE_USER exists
        userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ROLE_USER);
                    return roleRepository.save(role);
                });
    }

    @Test
    public void testSignup() {
        // Create signup request with required fields
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(testUsername);
        signupRequest.setEmail(testEmail);
        signupRequest.setPassword(testPassword);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignupRequest> request = new HttpEntity<>(signupRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signup",
                request,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isEqualTo("User registered successfully! Please check your email to activate your account!");

        // Verify user exists in database
        Optional<User> createdUser = userRepository.getByUsername(testUsername);
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    public void testSignin() {
        // First ensure ROLE_USER exists
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(ERole.ROLE_USER);
                    return roleRepository.save(newRole);
                });

        // Create a user with all required fields
        User user = new User();
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(testPassword)); // encoded "Test123!"
        user.setPhoneNumber("1234567890"); // Required field
        user.setRoles(Set.of(userRole));   // Set user role
        userRepository.save(user);

        // Login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(testUsername);
        loginRequest.setPassword(testPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signin",
                request,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("token", "refreshToken", "id", "username", "email");

        // Save refresh token for next test
        refreshTokenValue = (String) response.getBody().get("refreshToken");
    }

    @Test
    public void testRefreshToken() {
        // First sign in to get a refresh token
        testSignin();

        // Ensure we have a refresh token
        assertThat(refreshTokenValue).isNotNull();

        // Create refresh token request
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(refreshTokenValue);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TokenRefreshRequest> request = new HttpEntity<>(refreshRequest, headers);

        ResponseEntity<TokenRefreshResponse> response = restTemplate.postForEntity(
                "/api/auth/refreshtoken",
                request,
                TokenRefreshResponse.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getRefreshToken()).isEqualTo(refreshTokenValue);
    }

    @Test
    public void testSignout() {
        // First sign in to get authentication
        testSignin();

        // Get token from response
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(testUsername);
        loginRequest.setPassword(testPassword);

        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, new HttpHeaders());
        ResponseEntity<Map> signinResponse = restTemplate.postForEntity(
                "/api/auth/signin",
                loginEntity,
                Map.class
        );

        String token = (String) signinResponse.getBody().get("token");

        // Sign out
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signout",
                request,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "User signed out successfully!");

        // Verify refresh token is deleted
        Optional<User> user = userRepository.getByUsername(testUsername);
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user.get());
        assertThat(refreshTokens).isEmpty();
    }

    @Test
    public void testForgotPassword() {
        // Create a user first
        User user = new User();
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setPassword("$2a$10$TJtUGBmxvfOYJ8EH/PKm0OyriQ3jYi7n2AZAfdyxGUZP7vvpFngz2");
        user.setPhoneNumber("1234567890");
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        // Request forgot password
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", testEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/forgotPassword",
                request,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    public void testInvalidCredentialsSignin() {
        // Login request with invalid credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("wrongpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signin",
                request,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testDuplicateUsernameSignup() {
        // Create a user first
        User user = new User();
        user.setUsername(testUsername);
        user.setEmail("another" + testEmail);
        user.setPassword("$2a$10$TJtUGBmxvfOYJ8EH/PKm0OyriQ3jYi7n2AZAfdyxGUZP7vvpFngz2");
        user.setPhoneNumber("1234567890");

        // Get role before using it to avoid NPE
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Required ROLE_USER not found"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        // Try to create another user with same username
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(testUsername);
        signupRequest.setEmail("unique" + testEmail);
        signupRequest.setPassword(testPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignupRequest> request = new HttpEntity<>(signupRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signup",
                request,
                Map.class
        );

        // Verify response indicates duplicate username
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message").toString()).contains("Username is already taken!");
    }
}