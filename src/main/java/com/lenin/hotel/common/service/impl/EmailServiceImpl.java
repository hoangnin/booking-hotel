package com.lenin.hotel.common.service.impl;

import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.common.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.lenin.hotel.common.utils.EmailTemplateUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    @Value("${hotel.frontEnd.host}")
    private String frontEndHostValue;
    @Value("${hotel.frontEnd.forgotPasswordPath}")
    private String frontEndForgotPasswordPathValue;
    @Value("${hotel.frontEnd.activeAccount}")
    private String frontEndActiveAccountValue;


    @Value("${spring.mail.username}")
    private String SENDER_EMAIL;

    @Value("${spring.application.name}")
    private String SERVICE_NAME;

    @Async
    public void sendEmailWithPdf(String to, String subject, String text, byte[] pdfData, String fileName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            helper.addAttachment(fileName, new ByteArrayResource(pdfData));

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    public void sendMailSignupSuccess(String username, String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Vinova - Register success");
            message.setFrom(SENDER_EMAIL);
            message.setTo(email);
            message.setText(signupSuccessEmail(username));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
    }

    @Async
    public void sendMailBookingSuccess(Booking booking) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Vinova - Booking success");
            message.setFrom(SENDER_EMAIL);
            message.setTo(booking.getUser().getEmail());
            message.setText(bookingSuccessEmail(booking));
            javaMailSender.send(message);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException();
        }
    }

    @Async
    public void sendMailForgotPassword(String username, String email, String tokenUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Vinova - Forgot Password");
            message.setFrom(SENDER_EMAIL);
            message.setTo(email);
            message.setText(forgotPasswordEmail(username, email, frontEndHostValue, frontEndForgotPasswordPathValue, tokenUrl));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
    }
    @Async
    public void sendMailActiveAccount(String username, String email, String tokenUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Vinova - Active Account");
            message.setFrom(SENDER_EMAIL);
            message.setTo(email);
            message.setText(activeAccountEmail(username, frontEndHostValue, frontEndActiveAccountValue, tokenUrl ));
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException();
        }
    }


}
