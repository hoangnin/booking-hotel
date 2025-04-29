package com.lenin.hotel.unit.service.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.dto.request.ReviewRequest;
import com.lenin.hotel.hotel.dto.response.ReviewResponse;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.Review;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.repository.ImageRepository;
import com.lenin.hotel.hotel.repository.ReviewRepository;
import com.lenin.hotel.hotel.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Hotel hotel;
    private ReviewRequest reviewRequest;



    @BeforeEach
    void setUp() {
        // Mock SecurityContext and Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn("testUser");

        // Set the mocked SecurityContext in SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        // Existing setup
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        hotel = new Hotel();
        hotel.setId(1);
        hotel.setRating(4.0);
        hotel.setTotalReview(10);

        reviewRequest = ReviewRequest.builder()
                .hotelId(1)
                .rating(5)
                .imageUrl("imageUrl")
                .content("Great hotel!")
                .build();
    }

    @Test
    void testCreateReview_Success() {
        // Arrange
        // Create a Review that should be returned by the mock repository
        Review savedReview = Review.builder()
                .id(1) // Important: provide an ID
                .rating(reviewRequest.getRating())
                .content(reviewRequest.getContent())
                .user(user)
                .hotel(hotel)
                .build();

        when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(user));
        when(bookingRepository.existsByUserAndHotelId(user, reviewRequest.getHotelId())).thenReturn(true);
        when(hotelRepository.getReferenceById(reviewRequest.getHotelId())).thenReturn(hotel);
        // Mock the save method to return our prepared review
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Act
        reviewService.createReview(reviewRequest);

        // Assert
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(hotelRepository, times(1)).save(hotel);
        assertEquals(4.09, hotel.getRating(), 0.01); // New average rating
        assertEquals(11, hotel.getTotalReview());
    }

    @Test
    void testCreateReview_UserNotFound() {
        // Arrange
        when(userRepository.getByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals("User not found!", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_NoBooking() {
        // Arrange
        when(userRepository.getByUsername(anyString())).thenReturn(Optional.of(user));
        when(bookingRepository.existsByUserAndHotelId(user, reviewRequest.getHotelId())).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.createReview(reviewRequest));
        assertEquals("You must booking a hotel first, then can leave a comment here!", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testGetReviewByHotel_Success() {
        // Arrange
        Review review = Review.builder()
                .id(1)
                .rating(5)
                .content("Great hotel!")
                .user(user)
                .hotel(hotel)
                .build();
        Image image = Image.builder().url("avatar.jpg").build();
        when(reviewRepository.findReviewsByHotelId(hotel.getId())).thenReturn(List.of(review));
        when(imageRepository.findByReferenceIdAndReferenceTableAndType(anyInt(), anyString(), eq(ImageType.HOTEL)))
                .thenReturn(List.of(image));

        // Act
        List<ReviewResponse> responses = reviewService.getReviewByHotel(hotel.getId());

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Great hotel!", responses.get(0).getContent());
        assertEquals("avatar.jpg", responses.get(0).getUserReview().getAvatar());
        verify(reviewRepository, times(1)).findReviewsByHotelId(hotel.getId());
    }

    @Test
    void testGetReviewByHotel_Empty() {
        // Arrange
        when(reviewRepository.findReviewsByHotelId(hotel.getId())).thenReturn(List.of());

        // Act
        List<ReviewResponse> responses = reviewService.getReviewByHotel(hotel.getId());

        // Assert
        assertTrue(responses.isEmpty());
        verify(reviewRepository, times(1)).findReviewsByHotelId(hotel.getId());
    }
}