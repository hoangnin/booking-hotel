package com.lenin.hotel.payment.controller;



import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.payment.dto.request.ChargeRequest;
import com.lenin.hotel.payment.service.IPaymentService;
import com.lenin.hotel.payment.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final IPaymentService paymentService;



    @GetMapping("/public/payment/payment-status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@RequestParam("session_id") String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("status", session.getPaymentStatus());
            response.put("customer_email", session.getCustomerEmail());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/public/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {
        try {
            paymentService.processStripeWebhook(request);
            return ResponseEntity.ok("Order updated successfully");
        } catch (com.lenin.hotel.common.exception.StripeException.InvalidSignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        } catch (com.lenin.hotel.common.exception.StripeException.InvalidPayloadException e) {
            return ResponseEntity.badRequest().body("Invalid payload");
        } catch (com.lenin.hotel.common.exception.StripeException.OrderUpdateFailedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update order");
        }
    }



    private final StripeService paymentsService;

    @PostMapping("/public/payment/charge")
    public ResponseEntity<Map<String, Object>> charge(@RequestBody ChargeRequest chargeRequest) throws StripeException {
        chargeRequest.setDescription("Example charge");
        chargeRequest.setCurrency(ChargeRequest.Currency.EUR);

        Charge charge = paymentsService.charge(chargeRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("id", charge.getId());
        response.put("status", charge.getStatus());
        response.put("chargeId", charge.getId());
        response.put("balance_transaction", charge.getBalanceTransaction());

        return ResponseEntity.ok(response);
    }


    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}