package com.lenin.hotel.unit.controller.payment;

import com.lenin.hotel.payment.controller.PaymentController;
import com.lenin.hotel.payment.dto.request.ChargeRequest;
import com.lenin.hotel.payment.service.IPaymentService;
import com.lenin.hotel.payment.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private IPaymentService paymentService;

    @Mock
    private StripeService stripeService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPaymentStatus_Success() throws StripeException {
        // Arrange
        String sessionId = "test_session_id";
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.getPaymentStatus()).thenReturn("paid");
        when(session.getCustomerEmail()).thenReturn("test@example.com");
        mockStatic(Session.class).when(() -> Session.retrieve(sessionId)).thenReturn(session);

        // Act
        ResponseEntity<Map<String, Object>> response = paymentController.getPaymentStatus(sessionId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionId, response.getBody().get("id"));
        assertEquals("paid", response.getBody().get("status"));
        assertEquals("test@example.com", response.getBody().get("customer_email"));
    }

    @Test
    void testHandleStripeWebhook_Success() {
        // Arrange
        doNothing().when(paymentService).processStripeWebhook(request);

        // Act
        ResponseEntity<String> response = paymentController.handleStripeWebhook(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order updated successfully", response.getBody());
        verify(paymentService, times(1)).processStripeWebhook(request);
    }

    @Test
    void testHandleStripeWebhook_InvalidSignature() {
        // Arrange
        doThrow(new com.lenin.hotel.common.exception.StripeException.InvalidSignatureException("Invalid signature"))
                .when(paymentService).processStripeWebhook(request);

        // Act
        ResponseEntity<String> response = paymentController.handleStripeWebhook(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid signature", response.getBody());
    }

    @Test
    void testCharge_Success() throws StripeException {
        // Arrange
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setAmount(1000);
        chargeRequest.setCurrency(ChargeRequest.Currency.USD);
        chargeRequest.setDescription("Test charge");

        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn("charge_id");
        when(charge.getStatus()).thenReturn("succeeded");
        when(charge.getBalanceTransaction()).thenReturn("txn_id");
        when(stripeService.charge(any(ChargeRequest.class))).thenReturn(charge);

        // Act
        ResponseEntity<Map<String, Object>> response = paymentController.charge(chargeRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("charge_id", response.getBody().get("id"));
        assertEquals("succeeded", response.getBody().get("status"));
        assertEquals("txn_id", response.getBody().get("balance_transaction"));
    }
}