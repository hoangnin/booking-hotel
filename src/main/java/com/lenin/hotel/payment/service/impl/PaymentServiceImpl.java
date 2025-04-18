package com.lenin.hotel.payment.service.impl;

import com.lenin.hotel.booking.enumuration.BookingStatus;
import com.lenin.hotel.common.exception.StripeException.*;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.payment.dto.request.ChargeRequest;
import com.lenin.hotel.payment.service.IPaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static com.lenin.hotel.hotel.utils.BookingUtils.calculateTotalBill;


@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    @Value("${STRIPE_PUBLIC_KEY}")
    private String stripePublicKey;

    @Value("${STRIPE_SIGNING_KEY}")
    private String stripeSigningKey;

    @Value("${hotel.frontEnd.host}")
    private String frontEndHost;

    private final BookingRepository bookingRepository;


    public void processStripeWebhook(HttpServletRequest request) {
        String payload = readRequestBody(request);
        String sigHeader = request.getHeader("Stripe-Signature");

        if (sigHeader == null || sigHeader.isEmpty()) {
            throw new InvalidSignatureException("Missing Stripe-Signature header");
        }

        Event event = verifyStripeSignature(payload, sigHeader);

        if ("checkout.session.completed".equals(event.getType()) && !handleCheckoutSessionCompleted(event)) {
            throw new OrderUpdateFailedException("Failed to update order");
        }
    }

    private String readRequestBody(HttpServletRequest request) {
        try {
            return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidPayloadException("Error reading webhook payload", e);
        }
    }

    private Event verifyStripeSignature(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, stripeSigningKey);
        } catch (SignatureVerificationException e) {
            throw new InvalidSignatureException("Invalid Stripe signature");
        }
    }

    private boolean handleCheckoutSessionCompleted(Event event) {
        return event.getDataObjectDeserializer().getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .map(session -> session.getMetadata().get("order_id"))
                .map(this::parseOrderId)
                .map(this::updateOrderStatus)
                .orElse(false);
    }

    private Integer parseOrderId(String orderIdStr) {
        try {
            return Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean updateOrderStatus(Integer orderId) {
        return orderId != null && bookingRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(order);
                    return true;
                }).orElse(false);
    }

    @Override
    public String createPayment(Booking booking) {
        try {
           Long amount = calculateTotalBill(booking);
            String currency = ChargeRequest.Currency.USD.toString();

            String successUrl = frontEndHost + "/payment/success";
            String cancelUrl = frontEndHost + "/payment/cancel";

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl)
                            .setCancelUrl(cancelUrl)
                            .putMetadata("order_id", booking.getId().toString())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(currency)
                                                            .setUnitAmount(amount)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("Hotel Booking Payment")
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            Session session = Session.create(params);

            return session.getUrl();
        } catch (StripeException e) {
            throw new BusinessException("Error while create payment process, please try again");
        }
    }
}
