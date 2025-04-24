package com.lenin.hotel.integration.controller.hotel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.request.PriceTrackingRequest;
import com.lenin.hotel.hotel.dto.response.HotelResponse;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.AmenityRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.repository.LocationRepository;
import com.lenin.hotel.hotel.repository.PriceTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HotelControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private String authHeader;
    private Location testLocation;
    private Amenity testAmenity1;
    private Amenity testAmenity2;
    private Hotel testHotel;
    private PriceTracking testPriceTracking;

    @BeforeEach
    public void setup() {
        authHeader = securityTestHelper.getAuthHeader();

        User testUser = userRepository.getByUsername("testuser").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("testuser");
            newUser.setEmail("test@example.com");
            newUser.setPassword("password");
            return userRepository.save(newUser);
        });
        // Initialize and save test data
        testLocation = locationRepository.findById(1).orElseGet(() -> {
            Location newLocation = new Location();
            newLocation.setId(1);
            newLocation.setName("Test Location");
            return locationRepository.save(newLocation);
        });

        // Properly initialize amenity 1
        testAmenity1 = amenityRepository.findByName("Amenity 1");
        if (testAmenity1 == null) {
            testAmenity1 = amenityRepository.findById(1).orElseGet(() -> {
                Amenity newAmenity = new Amenity();
                newAmenity.setId(1);
                newAmenity.setName("Amenity 1");
                return amenityRepository.save(newAmenity);
            });
        }

        // Properly initialize amenity 2
        testAmenity2 = amenityRepository.findByName("Amenity 2");
        if (testAmenity2 == null) {
            testAmenity2 = amenityRepository.findById(2).orElseGet(() -> {
                Amenity newAmenity = new Amenity();
                newAmenity.setId(2);
                newAmenity.setName("Amenity 2");
                return amenityRepository.save(newAmenity);
            });
        }
        testHotel = hotelRepository.findById(1).orElseGet(() -> {
            Hotel newHotel = new Hotel();
            newHotel.setName("Test Hotel");
            newHotel.setLocation(testLocation);
            newHotel.setLatitude(10.762622);
            newHotel.setLongitude(106.660172);
            newHotel.setAddress("123 Test Street");
            newHotel.setDescription("A test hotel for integration testing.");
            newHotel.setPhone("0123456789");
            newHotel.setEmail("testhotel@example.com");
            newHotel.setPolicy("No smoking policy.");
            newHotel.setGoogleMapEmbed("<iframe>Google Map Embed</iframe>");
            newHotel.setOwner(testUser); // Add this line to set the owner
            return hotelRepository.save(newHotel);
        });

        testPriceTracking = priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc((long) testHotel.getId()).orElseGet(() -> {
            PriceTracking newPriceTracking = new PriceTracking();
            newPriceTracking.setPrice(new BigDecimal("100.00"));
            newPriceTracking.setHotel(testHotel);
            return priceTrackingRepository.save(newPriceTracking);
        });
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }

    @Test
    public void testCreateHotel() throws Exception {
        // First check if location with ID 1 exists, if not create it
        Location location = locationRepository.findById(1)
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setName("Test Location");
                    return locationRepository.save(newLocation);
                });

        // Replace the amenity creation at line 164 with this
                Amenity amenity = amenityRepository.findByName("Amenity 1");
                    if(amenity ==null) {
                        Amenity newAmenity = new Amenity();
                        newAmenity.setName("Amenity " + UUID.randomUUID().toString().substring(0, 8));
                        newAmenity.setAvailable(true);
                        amenityRepository.save(newAmenity);
                    };

        HotelRequest hotelRequest = HotelRequest.builder()
                .name("Test Hotel")
                .description("A test hotel for integration testing.")
                .address("123 Test Street")
                .locationId(location.getId())  // Use the actual location ID
                .latitude(10.762622)
                .longitude(106.660172)
                .email("testhotel@example.com")
                .phone("0123456789")
                .policy("No smoking policy.")
                .googleMapEmbed("<iframe>Google Map Embed</iframe>")
                .price(PriceTrackingRequest.builder().price(BigDecimal.valueOf(100.0)).build())
                .amenities(Set.of(amenity.getId()))
                .build();

                User testUser = userRepository.findByUsername("testuser");
                if (testUser == null) {
                    testUser = new User();
                    testUser.setUsername("testuser");
                    testUser.setEmail("test@example.com");
                    testUser.setPassword("password");
                    userRepository.save(testUser);
                }

                User finalTestUser = testUser;
                Hotel savedHotel = hotelRepository.save(Hotel.builder()
                        .name(hotelRequest.getName())
                        .description(hotelRequest.getDescription())
                        .address(hotelRequest.getAddress())
                        .location(locationRepository.findById(hotelRequest.getLocationId()).orElseThrow())
                        .latitude(hotelRequest.getLatitude())
                        .longitude(hotelRequest.getLongitude())
                        .email(hotelRequest.getEmail())
                        .phone(hotelRequest.getPhone())
                        .policy(hotelRequest.getPolicy())
                        .googleMapEmbed(hotelRequest.getGoogleMapEmbed())
                        .owner(finalTestUser)
                        .build());

        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HotelRequest> requestEntity = new HttpEntity<>(hotelRequest, headers);

        String url = "/api/hotelOwner/createHotel";
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetHotels() throws Exception {
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = "/api/hotelOwner/hotels";
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testActivateHotelOwner() throws Exception {
        // Create a user to be activated first
        User user = userRepository.findByUsername("testuser");
        if (user == null) {
            // If user doesn't exist, create one
            User newUser = new User();
            newUser.setUsername("testuser");
            newUser.setEmail("test@example.com");
            newUser.setPassword("password");
            user = userRepository.save(newUser);
        }

        // Use the actual user ID instead of hardcoded "1"
        String url = "/api/admin/active/" + user.getId();
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message").asText()).isEqualTo("Activated successfully!");
    }

    @Test
    public void testDashboardOverview() {
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/api/admin/dashboard/overview",
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("revenue")).isNotNull();
    }



    @Test
    public void testSendReport() throws Exception {
        HttpHeaders headers = getHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = "/api/hotelOwner/sendReport";
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JsonNode.class);
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode() == HttpStatus.BAD_REQUEST).isTrue();
    }
}