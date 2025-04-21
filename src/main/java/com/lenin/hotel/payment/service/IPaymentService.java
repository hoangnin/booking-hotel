package com.lenin.hotel.payment.service;

import com.lenin.hotel.hotel.model.Booking;
import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    void processStripeWebhook(HttpServletRequest request);
    String createPayment(Booking booking);
}
