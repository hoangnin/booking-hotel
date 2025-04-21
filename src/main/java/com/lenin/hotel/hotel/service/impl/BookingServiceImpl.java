package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.common.PagedResponse;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.service.IEmailService;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;
import com.lenin.hotel.hotel.service.IBookingService;
import com.lenin.hotel.hotel.service.IHotelService;
import com.lenin.hotel.hotel.utils.BookingUtils;
import com.lenin.hotel.payment.service.IPaymentService;
import com.lenin.hotel.payment.service.impl.PaymentServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.lenin.hotel.common.utils.SecurityUtils.getCurrentUsername;
import static com.lenin.hotel.hotel.utils.BookingUtils.buildBookingEntity;
import static com.lenin.hotel.hotel.utils.BookingUtils.buildBookingResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final IHotelService hotelService;
    private final IEmailService IEmailService;
    private final IPaymentService checkoutService;
    private final PaymentServiceImpl paymentServiceImpl;

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        Booking booking = buildBookingEntity(bookingRequest);
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() ->new RuntimeException("User not found"));
        booking.setUser(user);
        Hotel hotel  = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() -> new RuntimeException("Hotel not found"));
        booking.setHotel(hotel);
        PriceTracking priceTracking = hotelService.getLatestPrice(Long.valueOf(bookingRequest.getHotelId()));
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

        IEmailService.sendMailBookingSuccess(booking);
        BookingResponse bookingResponse = buildBookingResponse(booking);

        //create payment url
        String paymentUrl = paymentServiceImpl.createPayment(booking);
        bookingResponse.setPaymentUrl(paymentUrl);
        return bookingResponse;
    }

    @Override
    public PagedResponse<BookingResponse> getBookingByUser(int page, int size) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new BusinessException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDt"));
        Page<Booking> bookingPage = bookingRepository.findAllByUser(user, pageable);
        return new PagedResponse<>(
                bookingPage.getContent().stream().map(BookingUtils::buildBookingResponse).toList(),
                bookingPage.getNumber(),
                bookingPage.getSize(),
                bookingPage.getTotalElements(),
                bookingPage.getTotalPages(),
                bookingPage.isLast()
        );
    }

    @Override
    public BookingResponse getBookingById(Integer id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return buildBookingResponse(booking);
    }
}
