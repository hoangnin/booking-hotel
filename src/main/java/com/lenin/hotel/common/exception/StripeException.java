package com.lenin.hotel.common.exception;

import com.stripe.exception.SignatureVerificationException;

import java.io.IOException;

public class StripeException extends RuntimeException {
    public StripeException(String message) {
        super(message);
    }
    public StripeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class InvalidSignatureException extends StripeException {
        public InvalidSignatureException(String message) {
            super(message);
        }
        public InvalidSignatureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidPayloadException extends StripeException {
        public InvalidPayloadException(String message, IOException e) {
            super(message);
        }
    }

    public static class OrderUpdateFailedException extends StripeException {
        public OrderUpdateFailedException(String message) {
            super(message);
        }
    }
}
