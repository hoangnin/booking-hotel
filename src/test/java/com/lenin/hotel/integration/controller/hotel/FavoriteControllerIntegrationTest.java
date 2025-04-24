package com.lenin.hotel.integration.controller.hotel;

import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.utils.SecurityUtils;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.configuration.SecurityTestConfig;
import com.lenin.hotel.configuration.SecurityTestHelper;
import com.lenin.hotel.configuration.TestDynamicProperties;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.hotel.repository.HotelRepository;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({DatabaseTestContainer.class, SecurityTestConfig.class})
public class FavoriteControllerIntegrationTest extends TestDynamicProperties {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;
    private String authHeader;

    @Autowired
    private SecurityTestHelper securityTestHelper;

    @BeforeEach
    public void setup() {
        authHeader = securityTestHelper.getAuthHeader();
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private Hotel createTestHotel() {
        Location location = new Location();
        location.setName("Test Location for Favorite");
        location = locationRepository.save(location);

        String currentUsername = SecurityUtils.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            // Find if a user with this email already exists
            String uniqueEmail = "dummy_favorite_" + System.currentTimeMillis() + "@example.com";
            currentUser = new User();
            currentUser.setUsername(currentUsername);
            currentUser.setEmail(uniqueEmail);
            currentUser = userRepository.save(currentUser);
        }

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel Favorite");
        hotel.setDescription("Test Description");
        hotel.setAddress("Test Address");
        hotel.setPhone("987654321");
        hotel.setEmail("test_favorite@example.com");
        hotel.setLatitude(10.0);
        hotel.setLongitude(20.0);
        hotel.setGoogleMapEmbed("<iframe></iframe>");
        hotel.setLocation(location);
        hotel.setOwner(currentUser);
        hotel = hotelRepository.save(hotel);
        return hotel;
    }

    @Test
    public void testAddFavorite() {
        Hotel hotel = createTestHotel();
        Integer hotelId = hotel.getId();
        assertThat(hotelId).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String addFavoriteUrl = UriComponentsBuilder.fromPath("/api/user/favorite/{hotelId}")
                .buildAndExpand(hotelId)
                .toString();
        ResponseEntity<?> addFavoriteResponse = restTemplate.exchange(addFavoriteUrl, HttpMethod.GET, entity, Object.class);
        assertThat(addFavoriteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetFavorites() {
        // Create a hotel and add it to favorites
        Hotel hotel = createTestHotel();
        Integer hotelId = hotel.getId();
        assertThat(hotelId).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // First add favorite
        String addFavoriteUrl = UriComponentsBuilder.fromPath("/api/user/favorite/{hotelId}")
                .buildAndExpand(hotelId)
                .toString();
        restTemplate.exchange(addFavoriteUrl, HttpMethod.GET, entity, Object.class);

        // Then fetch favorites
        String getFavoritesUrl = UriComponentsBuilder.fromPath("/api/user/favorite")
                .toUriString();
        ResponseEntity<Integer[]> getFavoritesResponse = restTemplate.exchange(getFavoritesUrl, HttpMethod.GET, entity, Integer[].class);
        assertThat(getFavoritesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Integer[] favorites = getFavoritesResponse.getBody();
        assertThat(favorites).isNotEmpty();

        boolean favFound = false;
        for (Integer favId : favorites) {
            if (favId.equals(hotelId)) {
                favFound = true;
                break;
            }
        }
        assertThat(favFound).isTrue();
    }

    @Test
    public void testRemoveFavorite() {
        // Create a hotel and add it as favorite
        Hotel hotel = createTestHotel();
        Integer hotelId = hotel.getId();
        assertThat(hotelId).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Add favorite
        String addFavoriteUrl = UriComponentsBuilder.fromPath("/api/user/favorite/{hotelId}")
                .buildAndExpand(hotelId)
                .toString();
        restTemplate.exchange(addFavoriteUrl, HttpMethod.GET, entity, Object.class);

        // Remove favorite
        String removeFavoriteUrl = UriComponentsBuilder.fromPath("/api/user/favorite/{hotelId}")
                .buildAndExpand(hotelId)
                .toString();
        ResponseEntity<?> removeFavoriteResponse = restTemplate.exchange(removeFavoriteUrl, HttpMethod.DELETE, entity, Object.class);
        assertThat(removeFavoriteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify removal
        String getFavoritesUrl = UriComponentsBuilder.fromPath("/api/user/favorite")
                .toUriString();
        ResponseEntity<Integer[]> getFavoritesResponse = restTemplate.exchange(getFavoritesUrl, HttpMethod.GET, entity, Integer[].class);
        assertThat(getFavoritesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Integer[] favoritesAfterRemove = getFavoritesResponse.getBody();
        boolean favExistsAfterRemoval = false;
        if (favoritesAfterRemove != null) {
            for (Integer favId : favoritesAfterRemove) {
                if (favId.equals(hotelId)) {
                    favExistsAfterRemoval = true;
                    break;
                }
            }
        }
        assertThat(favExistsAfterRemoval).isFalse();
    }
}