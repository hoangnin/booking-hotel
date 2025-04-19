package com.lenin.hotel.common.utils;



import com.lenin.hotel.booking.model.Booking;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
@Slf4j
public class EmailTemplateUtils {
    public static String bookingSuccessEmail(Booking booking) {
        return String.format(
                "Subject: 🏨 Booking Confirmation - %s\n\n" +
                        "Dear %s,\n\n" +
                        "We are pleased to confirm your booking at **%s**!\n\n" +
                        "📅 Check-in Date: %s\n" +
                        "📅 Check-out Date: %s\n\n" +
                        "Booking Details:\n" +
                        "- Booking ID: #%d\n" +
                        "- Status: %s\n" +
                        "- Special Notes: %s\n\n" +
                        "📍 Hotel Location: %s\n\n" +
                        "💰 Price Tracking Reference: %s\n\n" +
                        "Thank you for choosing Vinova for your stay! If you have any questions, feel free to contact us.\n\n" +
                        "Best regards,\n" +
                        "Vinova Team\n",
                booking.getHotel().getName(),  // Subject includes hotel name
                booking.getUser().getUsername(),  // Greeting
                booking.getHotel().getName(),  // Hotel Name
                booking.getCheckIn().toLocalDate(),  // Check-in Date
                booking.getCheckOut().toLocalDate(),  // Check-out Date
                booking.getId(),  // Booking ID
                booking.getStatus(),  // Booking Status
                booking.getNote() != null ? booking.getNote() : "N/A",  // Special Notes (if any)
                booking.getHotel().getAddress(),  // Hotel Location (Assuming Hotel has `getLocation()`)
                booking.getPriceTracking().getId()  // Price Tracking Reference
        );
    }

    public static String signupSuccessEmail(String username) {
        return String.format(
                "Subject: 🎉 Welcome to Vinova, %s!\n\n" +
                        "Hello %s,\n\n" +
                        "We're thrilled to have you join Vinova! Your account has been successfully created, and you're now part of our community.\n\n" +
                        "---\n\n" +
                        "Account Details:\n" +
                        "- Username: %s\n" +
                        "- Email: %s\n\n" +
                        "What’s next?\n\n" +
                        "✅ Explore our products and enjoy exclusive member benefits.\n" +
                        "✅ Manage your orders and preferences in your account dashboard.\n" +
                        "✅ Stay updated with the latest deals and promotions.\n\n" +
                        "If you have any questions or need assistance, feel free to contact us at [gillkaijame@gmail.com].\n\n" +
                        "Thank you for choosing Vinova! We look forward to serving you.\n\n" +
                        "Best regards,\n" +
                        "Vinova Team\n",
                username,username,username,username
        );
    }
    public static String forgotPasswordEmail(String username, String email, String frontEndHost, String frontEndForgotPasswordPath, String token) {
        // Log các tham số đầu vào
        log.info("Generating forgot password email content with the following parameters:");
        log.info("Username: {}", username);
        log.info("Email: {}", email);
        log.info("Frontend Host: {}", frontEndHost);
        log.info("Forgot Password Path: {}", frontEndForgotPasswordPath);
        log.info("Token: {}", token);

        // Kiểm tra nếu có tham số null
        if (username == null || email == null || frontEndHost == null || frontEndForgotPasswordPath == null || token == null) {
            log.error("One or more parameters are null in forgotPasswordEmail: username={}, email={}, frontEndHost={}, frontEndForgotPasswordPath={}, token={}",
                    username, email, frontEndHost, frontEndForgotPasswordPath, token);
            throw new IllegalArgumentException("One or more parameters are null in forgotPasswordEmail()");
        }

        // Tạo nội dung email
        String emailContent = String.format(
                "Subject: 🔒 Reset Your Password - Vinova\n\n" +
                        "Hello %s,\n\n" +
                        "We received a request to reset your password for your Vinova account associated with %s.\n\n" +
                        "To reset your password, please click the link below:\n\n" +
                        "🔗 %s/%s?token=%s\n\n" +
                        "This link is valid for a limited time. If you did not request a password reset, please ignore this email.\n\n" +
                        "For security reasons, never share your password with anyone.\n\n" +
                        "If you need further assistance, feel free to contact us at [gillkaijame@gmail.com].\n\n" +
                        "Best regards,\n" +
                        "Vinova Team\n",
                username, email, frontEndHost, frontEndForgotPasswordPath, token
        );

        // Log nội dung email
        log.info("Generated email content: \n{}", emailContent);

        return emailContent;
    }


    public static String activeAccountEmail(String username, String frontEndHost, String frontEndActivationPath, String token) {
        return String.format(
                "Subject: 🎉 Activate Your Account - Vinova\n\n" +
                        "Hello %s,\n\n" +
                        "Thank you for registering an account with Vinova!\n\n" +
                        "To activate your account, please click the link below:\n\n" +
                        "🔗 %s/%s?token=%s\n\n" +
                        "If you did not sign up for a Vinova account, please ignore this email.\n\n" +
                        "For security reasons, never share your activation link with anyone.\n\n" +
                        "If you need further assistance, feel free to contact us at [gillkaijame@gmail.com].\n\n" +
                        "Best regards,\n" +
                        "Vinova Team\n",
                username, frontEndHost, frontEndActivationPath, token
        );
    }



}

