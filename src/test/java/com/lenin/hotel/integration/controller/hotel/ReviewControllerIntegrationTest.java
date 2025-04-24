package com.lenin.hotel.integration.controller.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.BookingStatus;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.repository.LocationRepository;
import com.lenin.hotel.hotel.repository.PriceTrackingRepository;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class ReviewControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    private String authHeader;
    private Hotel testHotel;
    private User testUser;

    @BeforeEach
    public void setup() {
        // Clean up database tables in correct order to avoid foreign key violations
        jdbcTemplate.execute("DELETE FROM reviews");
        jdbcTemplate.execute("DELETE FROM bookings");
        jdbcTemplate.execute("DELETE FROM price_tracking");
        jdbcTemplate.execute("DELETE FROM hotel_amenity");
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM images WHERE reference_table = 'hotels'");
        jdbcTemplate.execute("DELETE FROM hotels");
        jdbcTemplate.execute("DELETE FROM locations");

        // First get the auth header and authenticated user
        authHeader = securityTestHelper.getAuthHeader();
        // Get the authenticated user rather than creating a new one
        testUser = userRepository.findByUsername(securityTestHelper.getTestUsername());

        // Create test location
        Location location = new Location();
        location.setName("Test Location");
        location = locationRepository.save(location);

        // Create test hotel
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setDescription("A hotel for testing");
        testHotel.setAddress("123 Test Street");
        testHotel.setRating(4.5);
        testHotel.setTotalReview(10);
        testHotel.setLatitude(10.0);
        testHotel.setLongitude(10.0);
        testHotel.setLocation(location);
        testHotel.setOwner(testUser);
        testHotel = hotelRepository.save(testHotel);

        // Create price tracking entry (needed for booking)
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setHotel(testHotel);
        priceTracking.setPrice(BigDecimal.valueOf(100));
        priceTracking = priceTrackingRepository.save(priceTracking);

        // Create test booking for the authenticated user
        Booking booking = new Booking();
        booking.setHotel(testHotel);
        booking.setUser(testUser); // Use the same user as the authenticated one
        booking.setCheckIn(ZonedDateTime.now().minusDays(10)); // Past booking
        booking.setCheckOut(ZonedDateTime.now().minusDays(5)); // Past checkout
        booking.setStatus(BookingStatus.CONFIRMED); // Booking must be completed to review
        booking.setPriceTracking(priceTracking);
        bookingRepository.save(booking);
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    public void testCreateReview() {
        // Create review request
        Map<String, Object> reviewRequest = new HashMap<>();
        reviewRequest.put("hotelId", testHotel.getId());
        reviewRequest.put("rating", 5);
        reviewRequest.put("content", "Great hotel, would stay again!");

        // Send POST request to create review
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reviewRequest, getAuthHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/user/review",
                request,
                Map.class
        );

        // Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Review created");
    }

    @Test
    public void testCreateReviewUnauthorized() {
        // Create review request without auth headers
        Map<String, Object> reviewRequest = new HashMap<>();
        reviewRequest.put("hotelId", testHotel.getId());
        reviewRequest.put("rating", 5);
        reviewRequest.put("content", "Great hotel!");

        // Send unauthorized POST request
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(reviewRequest);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/user/review",
                request,
                Map.class
        );

        // Verify unauthorized response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testGetReviewsByHotel() {
        // First create a review
        testCreateReview();

        // Get reviews for the hotel
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/public/review/" + testHotel.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0)).containsKey("rating");
        assertThat(response.getBody().get(0)).containsKey("content");
    }

    @Test
    public void testGetReviewsByHotelWhenEmpty() {
        // Get reviews for a hotel with no reviews
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/public/review/" + testHotel.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );

        // Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}