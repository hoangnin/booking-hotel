// File: src/test/java/com/lenin/hotel/integration/controller/hotel/AmenityControllerIntegrationTest.java
package com.lenin.hotel.integration.controller.hotel;

import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.dto.request.AmenityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
public class AmenityControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    private String authHeader;

    @BeforeEach
    public void setup() {
        authHeader = securityTestHelper.getAuthHeader();
    }

    @Test
    public void testCreateAmenity() {
        AmenityRequest amenityRequest = new AmenityRequest();
        amenityRequest.setName("Pool");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<AmenityRequest> entity = new HttpEntity<>(amenityRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/createAmenity", entity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = response.getBody();
        assertThat(body).containsEntry("message", "create amenity successfully!");
    }

    @Test
    public void testGetAllAmenities() {
        AmenityRequest amenityRequest = new AmenityRequest();
        amenityRequest.setName("Gym");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<AmenityRequest> entity = new HttpEntity<>(amenityRequest, headers);
        restTemplate.postForEntity("/api/admin/createAmenity", entity, Map.class);

        ResponseEntity<List> response = restTemplate.getForEntity("/api/public/amenity", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> amenities = response.getBody();
        assertThat(amenities).isNotEmpty();
    }
}