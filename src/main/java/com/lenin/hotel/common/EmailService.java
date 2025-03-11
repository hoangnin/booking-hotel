package com.lenin.hotel.common;


public interface EmailService {


    public void sendMailSignupSuccess(String username, String email);
    public void sendMailForgotPassword(String username, String email, String tokenUrl);
    public void sendMailActiveAccount(String username, String email, String tokenUrl);
}
