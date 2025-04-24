package com.lenin.hotel.integration.controller.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.utils.SecurityUtils;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.repository.PriceTrackingRepository;
import com.lenin.hotel.hotel.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
public class BookingControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    private String authHeader;

    @BeforeEach
    public void setup() {
        authHeader = securityTestHelper.getAuthHeader();
        // Set a dummy authentication to handle SecurityUtils.getCurrentUsername()
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    public void testCreateBooking() {
        Location location = new Location();
        location.setName("Test Location");
        location = locationRepository.save(location);

        String currentUsername = SecurityUtils.getCurrentUsername();
        User owner = userRepository.findByUsername(currentUsername);
        if (owner == null) {
            owner = new User();
            owner.setUsername(currentUsername);
            owner.setEmail("dummy@example.com");
            owner = userRepository.save(owner);
        }

        // Build hotel record with required fields
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setDescription("Test Description");
        hotel.setAddress("Test Address");
        hotel.setPhone("123456789");
        hotel.setEmail("test@example.com");
        hotel.setLatitude(100.0);
        hotel.setLongitude(200.0);
        hotel.setGoogleMapEmbed("<iframe></iframe>");
        hotel.setLocation(location);
        hotel.setOwner(owner);
        hotel = hotelRepository.save(hotel);

        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(100));
        priceTracking.setHotel(hotel);
        priceTracking = priceTrackingRepository.save(priceTracking);

        BookingRequest bookingRequest = BookingRequest.builder()
                .hotelId(hotel.getId())
                .note("Test booking note")
                .checkIn(ZonedDateTime.now().plusDays(1))
                .checkOut(ZonedDateTime.now().plusDays(3))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingRequest> entity = new HttpEntity<>(bookingRequest, headers);

        ResponseEntity<BookingResponse> response = restTemplate.postForEntity(
                "/api/user/booking", entity, BookingResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookingResponse bookingResponse = response.getBody();
        assertThat(bookingResponse).isNotNull();
        assertThat(bookingResponse.getPaymentUrl()).isNotNull();
    }

    @Test
    public void testGetBookingByUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user/booking?page=0&size=10", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}