package com.lenin.hotel.repository.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.model.Review;
import com.lenin.hotel.hotel.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private Hotel hotel;
    private User user;

   @BeforeEach
    public void setUp() {
        // Create and persist a Location
        Location location = Location.builder()
                .name("Test Location")
                .build();
        location = entityManager.persistAndFlush(location);

        // Create and persist a User (acting as the Owner)
        user = new User();
        user.setUsername("Test Owner");
        user.setEmail("owner@example.com");
        user.setPassword("securePassword123"); // Set a valid password
        user = entityManager.persistAndFlush(user);

        // Create and persist a Hotel
        hotel = Hotel.builder()
                .name("Test Hotel")
                .latitude(10.762622)
                .longitude(106.660172)
                .location(location) // Assign the location
                .owner(user)        // Assign the owner
                .build();
        hotel = entityManager.persistAndFlush(hotel);

        // Create and persist Reviews
        Review review1 = Review.builder()
                .rating(5)
                .content("Excellent service!")
                .hotel(hotel)
                .user(user)
                .build();
        reviewRepository.save(review1);

        Review review2 = Review.builder()
                .rating(4)
                .content("Very good experience.")
                .hotel(hotel)
                .user(user)
                .build();
        reviewRepository.save(review2);
    }

    @Test
    public void testFindReviewsByHotelId() {
        // Act
        List<Review> reviews = reviewRepository.findReviewsByHotelId(hotel.getId());

        // Assert
        assertThat(reviews).isNotEmpty();
        assertThat(reviews.size()).isEqualTo(2);
        assertThat(reviews.get(0).getHotel().getId()).isEqualTo(hotel.getId());
    }

    @Test
    public void testFindReviewsByHotelId_NoResults() {
        // Act
        List<Review> reviews = reviewRepository.findReviewsByHotelId(999);

        // Assert
        assertThat(reviews).isEmpty();
    }
}