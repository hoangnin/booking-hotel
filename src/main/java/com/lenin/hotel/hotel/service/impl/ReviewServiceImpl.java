package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.dto.request.ImageRequest;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.Review;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.repository.ImageRepository;
import com.lenin.hotel.hotel.repository.ReviewRepository;
import com.lenin.hotel.hotel.dto.request.ReviewRequest;
import com.lenin.hotel.hotel.dto.response.ReviewResponse;
import com.lenin.hotel.hotel.dto.response.UserReviewResponse;
import com.lenin.hotel.hotel.service.IReviewService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lenin.hotel.common.utils.SecurityUtils.getCurrentUsername;
import static com.lenin.hotel.hotel.utils.HotelUtils.buildReviewResponse;
import static com.lenin.hotel.hotel.utils.ImageUtils.buildImage;

@Service
@Transactional
public class ReviewServiceImpl implements IReviewService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;

    public ReviewServiceImpl(UserRepository userRepository, BookingRepository bookingRepository, HotelRepository hotelRepository, ReviewRepository reviewRepository, ImageRepository imageRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public void createReview(ReviewRequest request) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(
                () -> new ResourceNotFoundException("User not found!"));
        if (!bookingRepository.existsByUserAndHotelId(user, request.getHotelId())) {
            throw new BusinessException("You must booking a hotel first, then can leave a comment here!");
        }
        Hotel hotel = hotelRepository.getReferenceById(request.getHotelId());
        Review review = reviewRepository.save(
                Review.builder()
                        .rating(request.getRating())
                        .content(request.getContent())
                        .user(user)
                        .hotel(hotel)
                        .build()
        );
        ImageRequest imageRequest = ImageRequest.builder()
                .url(request.getImageUrl())
                .type(ImageType.COMMENT)
                .build();
        Image newImage = buildImage(imageRequest, review.getId().intValue(), "reviews");
        imageRepository.save(newImage);

        int totalReviews = hotel.getTotalReview();
        double newRating = (hotel.getRating() * totalReviews + request.getRating()) / (totalReviews + 1); // Tính trung bình mới
        hotel.setRating(newRating);
        hotel.setTotalReview(totalReviews + 1);
        hotelRepository.save(hotel);


    }

    @Override
    public List<ReviewResponse> getReviewByHotel(Integer hotelId) {
        List<Review> reviews = reviewRepository.findReviewsByHotelId(hotelId);
        return reviews.stream().map(
                review -> {
                    ReviewResponse response = buildReviewResponse(review);
                    Image imageAvatar = imageRepository.findByReferenceIdAndReferenceTableAndType(Math.toIntExact(review.getUser().getId()), "users", ImageType.HOTEL).stream().findFirst().orElse(null);
                    Image imageComment = imageRepository.findByReferenceIdAndReferenceTableAndType(Math.toIntExact(review.getId()), "reviews", ImageType.COMMENT).stream().findFirst().orElse(null);
                    response.setUserReview(UserReviewResponse.builder()
                            .avatar(imageAvatar != null ? imageAvatar.getUrl() : null)
                            .username(review.getUser().getUsername())
                            .build());
                    response.setUrl(imageComment != null ? imageComment.getUrl() : null);
                    return response;
                }).toList();
    }

}
