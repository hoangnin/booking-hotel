package com.lenin.hotel.integration.controller.hotel;

import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.repository.LocationRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class LocationControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String authHeader;

    @BeforeEach
    public void setup() {
        authHeader = securityTestHelper.getAuthHeader();

        // Clean up database in proper order to respect foreign key constraints
        // The correct order is crucial to avoid constraint violations
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM hotel_amenity");
        jdbcTemplate.execute("DELETE FROM reviews");
        // Delete bookings before price_tracking since bookings reference price_tracking
        jdbcTemplate.execute("DELETE FROM bookings");
        jdbcTemplate.execute("DELETE FROM price_tracking");
        jdbcTemplate.execute("DELETE FROM images WHERE reference_table = 'hotels'");
        jdbcTemplate.execute("DELETE FROM hotels");
        jdbcTemplate.execute("DELETE FROM locations");
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    public void testCreateLocation() {
        // Prepare the request
        Map<String, String> locationRequest = new HashMap<>();
        locationRequest.put("name", "Test Location");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(locationRequest, getAuthHeaders());

        // Send the request
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/admin/location/create",
                entity,
                Map.class
        );

        // Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Location created successfully");

        // Verify the location was created in the database
        assertThat(locationRepository.existsByName("Test Location")).isTrue();
    }

    @Test
    public void testCreateDuplicateLocation() {
        // Create a location first
        Map<String, String> locationRequest = new HashMap<>();
        locationRequest.put("name", "Duplicate Location");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(locationRequest, getAuthHeaders());
        restTemplate.postForEntity("/api/admin/location/create", entity, Map.class);

        // Try to create the same location again
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/admin/location/create",
                entity,
                Map.class
        );

        // Verify the response indicates an error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetAllLocations() {
        // Create a location first
        Map<String, String> locationRequest = new HashMap<>();
        locationRequest.put("name", "Location for GetAll Test");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(locationRequest, getAuthHeaders());
        restTemplate.postForEntity("/api/admin/location/create", entity, Map.class);

        // Get all locations
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/public/location",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        // Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0)).containsKey("id");
        assertThat(response.getBody().get(0)).containsEntry("name", "Location for GetAll Test");
    }

    @Test
    public void testGetAllLocationsWhenEmpty() {
        // Get all locations
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/public/location",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        // Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void testCreateLocationUnauthorized() {
        // Prepare the request without auth header
        Map<String, String> locationRequest = new HashMap<>();
        locationRequest.put("name", "Unauthorized Location");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(locationRequest, headers);

        // Send the request
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/admin/location/create",
                entity,
                Map.class
        );

        // Verify the response indicates unauthorized access
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}