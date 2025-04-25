package com.lenin.hotel.integration.controller.authentication;

import com.lenin.hotel.authentication.dto.request.BlockRequest;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.PagedResponse;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminAuthHeader;
    private User testUser;

    @BeforeEach
    public void setup() {
        try {
            // Clean up database in correct order to respect foreign key constraints
            jdbcTemplate.execute("DELETE FROM price_tracking");
            jdbcTemplate.execute("DELETE FROM hotel_amenity");
            jdbcTemplate.execute("DELETE FROM hotels");
            jdbcTemplate.execute("DELETE FROM user_role WHERE user_id != (SELECT id FROM users WHERE username = 'admin')");
            jdbcTemplate.execute("DELETE FROM users WHERE username != 'admin'");
        } catch (Exception e) {
            System.err.println("Database cleanup error: " + e.getMessage());
        }

        // Get admin authentication header
        adminAuthHeader = securityTestHelper.getAuthHeader();

        // Create test user with unique identifiers
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        testUser = new User();
        testUser.setUsername("testuser_" + uniqueSuffix);
        testUser.setEmail("testuser_" + uniqueSuffix + "@example.com");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test Street");

        // Get ROLE_USER from repository
        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_USER);
        if (userRole.isPresent()) {
            testUser.setRoles(Set.of(userRole.get()));
        }

        testUser = userRepository.save(testUser);
    }

    private HttpHeaders getAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminAuthHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    public void testBlockUser() {
        // Prepare block request
        BlockRequest blockRequest = new BlockRequest();
        blockRequest.setUserId(testUser.getId());
        blockRequest.setBanReason("Violated terms of service");

        // Send request
        HttpEntity<BlockRequest> requestEntity = new HttpEntity<>(blockRequest, getAdminHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/admin/block",
                requestEntity,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Ban user Success");

        // Verify user is blocked in database
        User blockedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(blockedUser.getBanReason()).isEqualTo("Violated terms of service");
    }

    @Test
    public void testUnblockUser() {
        // First block the user using direct JDBC update to avoid Hibernate collection issues
        jdbcTemplate.update(
                "UPDATE users SET ban_reason = ? WHERE id = ?",
                "Test ban reason",
                testUser.getId()
        );

        // Send unblock request
        HttpEntity<?> requestEntity = new HttpEntity<>(getAdminHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/admin/unBlock/" + testUser.getId(),
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Un ban user Success");

        // Verify user is unblocked in database
        User unblockedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(unblockedUser.getBanReason()).isNull();
    }

    @Test
    public void testGetUsers() {
        // Send request to get users
        HttpEntity<?> requestEntity = new HttpEntity<>(getAdminHeaders());
        ResponseEntity<PagedResponse<UserResponse>> response = restTemplate.exchange(
                "/api/admin/users?page=0&size=10",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<PagedResponse<UserResponse>>() {
                }
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isNotEmpty();

        // Verify our test user is in the response
        boolean foundTestUser = response.getBody().content().stream()
                .anyMatch(user -> user.getUsername().equals(testUser.getUsername()));
        assertThat(foundTestUser).isTrue();
    }

    @Test
    public void testSearchUsers() {
        // Send request to search users
        HttpEntity<?> requestEntity = new HttpEntity<>(getAdminHeaders());
        ResponseEntity<List<UserResponse>> response = restTemplate.exchange(
                "/api/admin/users/search?keySearch=testuser",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserResponse>>() {
                }
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();

        // Verify our test user is found in search results
        boolean foundTestUser = response.getBody().stream()
                .anyMatch(user -> user.getUsername().equals(testUser.getUsername()));
        assertThat(foundTestUser).isTrue();
    }

    @Test
    public void testUnauthorizedAccess() {
        // Attempt to access users endpoint without authentication
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/admin/users",
                Map.class
        );

        // Verify unauthorized response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}