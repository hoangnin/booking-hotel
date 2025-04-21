package com.lenin.hotel.common.service;


import com.lenin.hotel.hotel.model.Booking;

public interface IEmailService {
    public void sendEmailWithPdf(String to, String subject, String text, byte[] pdfData, String fileName);
    public void sendMailBookingSuccess(Booking booking);
    public void sendMailSignupSuccess(String username, String email);
    public void sendMailForgotPassword(String username, String email, String tokenUrl);
    public void sendMailActiveAccount(String username, String email, String tokenUrl);
}
