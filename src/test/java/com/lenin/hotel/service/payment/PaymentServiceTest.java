package com.lenin.hotel.service.payment;

import com.lenin.hotel.common.enumuration.BookingStatus;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.StripeException.InvalidPayloadException;
import com.lenin.hotel.common.exception.StripeException.InvalidSignatureException;
import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.payment.service.impl.PaymentServiceImpl;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.lenin.hotel.hotel.utils.BookingUtils.calculateTotalBill;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentServiceImpl paymentService;


    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = spy(new PaymentServiceImpl(bookingRepository));
        ReflectionTestUtils.setField(paymentService, "stripeSigningKey", "stripe-signing-key");
    }

    @Test
    void testCreatePayment_Success() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setCheckIn(ZonedDateTime.now());
        booking.setCheckOut(ZonedDateTime.now().plusDays(2)); // Ensure checkOut is after checkIn

        // Initialize PriceTracking and set a price
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(100)); // Set a valid price
        booking.setPriceTracking(priceTracking);

        String expectedUrl = "https://example.com/payment/session";
        Session session = mock(Session.class);
        when(session.getUrl()).thenReturn(expectedUrl);

        try (var mockedStatic = mockStatic(Session.class)) {
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            String paymentUrl = paymentService.createPayment(booking);

            assertEquals(expectedUrl, paymentUrl);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testCreatePayment_StripeException() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setCheckIn(ZonedDateTime.now());
        booking.setCheckOut(ZonedDateTime.now().plusDays(2)); // Ensure checkOut is after checkIn

        // Initialize PriceTracking and set a price
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(100)); // Set a valid price
        booking.setPriceTracking(priceTracking);

        try (var mockedStatic = mockStatic(Session.class)) {
            mockedStatic.when(() -> Session.create(any(SessionCreateParams.class))).thenThrow(new BusinessException("Stripe error"));

            assertThrows(BusinessException.class, () -> paymentService.createPayment(booking));
        }
    }

    @Test
    void testProcessStripeWebhook_Success() throws IOException {
        String payload = "{\"type\":\"checkout.session.completed\"}";
        String sigHeader = "valid-signature";

        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload.getBytes());

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {
                // No implementation needed for this test
            }
        });
        when(request.getHeader("Stripe-Signature")).thenReturn(sigHeader);

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("checkout.session.completed");

        // Mock the EventDataObjectDeserializer and ensure it returns a mock Session.
        com.stripe.model.EventDataObjectDeserializer deserializer =
                mock(com.stripe.model.EventDataObjectDeserializer.class);
        Session session = mock(Session.class);
        // Set metadata containing the order_id
        when(session.getMetadata()).thenReturn(Map.of("order_id", "1"));
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        try (var mockedStatic = mockStatic(Webhook.class)) {
            mockedStatic.when(() -> Webhook.constructEvent(payload, sigHeader, "stripe-signing-key"))
                    .thenReturn(event);

            Booking booking = new Booking();
            booking.setId(1);
            booking.setStatus(BookingStatus.PENDING);

            when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

            paymentService.processStripeWebhook(request);

            assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
            verify(bookingRepository, times(1)).save(booking);
        }
    }

    @Test
    void testProcessStripeWebhook_InvalidSignature() throws IOException {
        String payload = "{\"type\":\"checkout.session.completed\"}";
        String sigHeader = "invalid-signature";

        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload.getBytes());

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {
                // No implementation needed for this test
            }
        });
        when(request.getHeader("Stripe-Signature")).thenReturn(sigHeader);

        try (var mockedStatic = mockStatic(Webhook.class)) {
            mockedStatic.when(() -> Webhook.constructEvent(payload, sigHeader, "stripe-signing-key"))
                    .thenThrow(new SignatureVerificationException("Invalid signature", null));

            assertThrows(InvalidSignatureException.class, () -> paymentService.processStripeWebhook(request));
        }
    }

    @Test
    void testProcessStripeWebhook_InvalidPayload() throws IOException {
        when(request.getInputStream()).thenThrow(new IOException("Error reading payload"));

        assertThrows(InvalidPayloadException.class, () -> paymentService.processStripeWebhook(request));
    }
}