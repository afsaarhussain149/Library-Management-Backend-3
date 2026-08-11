package com.library.services;

public interface EmailService {

    void sendOtpEmail(String toEmail, String userName, String otp);
}