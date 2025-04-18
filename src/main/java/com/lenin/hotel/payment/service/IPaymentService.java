package com.lenin.hotel.payment.service;

import com.lenin.hotel.booking.model.Booking;
import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    void processStripeWebhook(HttpServletRequest request);
    String createPayment(Booking booking);
}
