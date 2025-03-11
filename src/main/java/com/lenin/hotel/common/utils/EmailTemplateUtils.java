package com.lenin.hotel.common.utils;



import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

public class EmailTemplateUtils {

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
        return String.format(
                "Subject: 🔒 Reset Your Password - Vinova\n\n" +
                        "Hello %s,\n\n" +
                        "We received a request to reset your password for your Vinova account associated with %s.\n\n" +
                        "To reset your password, please click the link below:\n\n" +
                        "🔗 %s/%s?%s\n\n" +
                        "This link is valid for a limited time. If you did not request a password reset, please ignore this email.\n\n" +
                        "For security reasons, never share your password with anyone.\n\n" +
                        "If you need further assistance, feel free to contact us at [gillkaijame@gmail.com].\n\n" +
                        "Best regards,\n" +
                        "Vinova Team\n",
                username, email, frontEndHost, frontEndForgotPasswordPath, token
        );
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

