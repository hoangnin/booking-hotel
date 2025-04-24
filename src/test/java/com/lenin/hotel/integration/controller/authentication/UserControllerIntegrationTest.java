package com.lenin.hotel.integration.controller.authentication;

import com.lenin.hotel.authentication.dto.request.ResetPasswordRequest;
import com.lenin.hotel.authentication.dto.request.UpdateProfileRequest;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String testUsername;
    private String testEmail;
    private String testPassword;
    private String jwtToken;
    private User testUser;

    @BeforeEach
    public void setup() {
        try {
            // Clean database tables in correct order to respect foreign key constraints
            jdbcTemplate.execute("DELETE FROM price_tracking");
            jdbcTemplate.execute("DELETE FROM hotels");
            jdbcTemplate.execute("DELETE FROM user_role WHERE user_id != 1");
            jdbcTemplate.execute("DELETE FROM users WHERE id != 1 AND username != 'admin'");
            jdbcTemplate.execute("DELETE FROM roles WHERE name != 'ROLE_ADMIN'");
        } catch (Exception e) {
            System.err.println("Database cleanup error: " + e.getMessage());
        }

        // Create test data with unique identifiers
        testUsername = "testuser" + UUID.randomUUID().toString().substring(0, 8);
        testEmail = "test" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        testPassword = "Test123!";

        // Ensure ROLE_USER exists
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ROLE_USER);
                    return roleRepository.save(role);
                });

        // Create test user
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setEmail(testEmail);
        testUser.setPassword(passwordEncoder.encode(testPassword));
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("Test Address");
        testUser.setRoles(Set.of(userRole));
        userRepository.save(testUser);

        // Authenticate to get JWT token
        jwtToken = authenticateAndGetToken();
    }

    private String authenticateAndGetToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> loginRequest = Map.of(
                "username", testUsername,
                "password", testPassword
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/signin",
                request,
                Map.class
        );

        return (String) response.getBody().get("token");
    }

    @Test
    public void testGetUserInfo() {
        // Setup headers with auth token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Call endpoint
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/user/info",
                HttpMethod.GET,
                requestEntity,
                UserResponse.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo(testUsername);
        assertThat(response.getBody().getEmail()).isEqualTo(testEmail);
    }

    @Test
    public void testResetPassword() {
        // Setup request body
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setPassword(testPassword);
        resetRequest.setNewPassword("NewPassword123!");
        resetRequest.setConfirmPassword("NewPassword123!");

        // Setup headers with auth token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<ResetPasswordRequest> requestEntity = new HttpEntity<>(resetRequest, headers);

        // Call endpoint
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/user/resetPassword",
                requestEntity,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Password changed successfully");

        // Verify password was changed - login with new password
        Map<String, String> loginRequest = Map.of(
                "username", testUsername,
                "password", "NewPassword123!"
        );

        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, new HttpHeaders());
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/signin",
                loginEntity,
                Map.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testResetPasswordWithWrongCurrentPassword() {
        // Setup request with wrong current password
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setPassword("WrongPassword123!");
        resetRequest.setNewPassword("NewPassword123!");
        resetRequest.setConfirmPassword("NewPassword123!");

        // Setup headers with auth token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<ResetPasswordRequest> requestEntity = new HttpEntity<>(resetRequest, headers);

        // Call endpoint
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/user/resetPassword",
                requestEntity,
                Map.class
        );

        // Verify error response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("Current password is incorrect");
    }



    @Test
    public void testUnauthorizedAccess() {
        // Call user info endpoint without authentication
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/user/info",
                Map.class
        );

        // Verify unauthorized response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}