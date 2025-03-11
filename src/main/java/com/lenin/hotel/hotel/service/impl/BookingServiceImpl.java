package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.request.BookingRequest;
import com.lenin.hotel.hotel.response.BookingResponse;
import com.lenin.hotel.hotel.service.IBookingService;
import com.lenin.hotel.hotel.service.IHotelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lenin.hotel.common.utils.SecurityUtils.getCurrentUsername;
import static com.lenin.hotel.hotel.utils.BookingUtils.buildBookingEntity;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final IHotelService hotelService;
    @Override
    public void createBooking(BookingRequest bookingRequest) {
        Booking booking = buildBookingEntity(bookingRequest);
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() ->new RuntimeException("User not found"));
        booking.setUser(user);
        Hotel hotel  = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() -> new RuntimeException("Hotel not found"));
        booking.setHotel(hotel);
        PriceTracking priceTracking = hotelService.getPriceTracking(bookingRequest.getHotelId());
        if (priceTracking == null) {
            throw new ResourceNotFoundException("Hotel not found");
        }
        booking.setPriceTracking(priceTracking);
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                bookingRequest.getHotelId(), bookingRequest.getCheckIn(), bookingRequest.getCheckOut()
        );

        if (isOverlapping) {
            throw new IllegalStateException("The selected dates are already booked.");
        }
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingResponse> getBookingByUserId(String userId) {
        return List.of();
    }
}
