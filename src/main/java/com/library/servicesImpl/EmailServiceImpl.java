package com.library.servicesImpl;

import com.library.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String userName, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Library Management - Password Reset OTP");

        String name = userName != null && !userName.trim().isEmpty()
                ? userName
                : "User";

        message.setText(
                "Hello " + name + ",\n\n" +

                "We received a request to reset your Library Management account password.\n\n" +

                "Your OTP is: " + otp + "\n\n" +

                "This OTP is valid for 5 minutes.\n" +

                "Do not share this OTP with anyone.\n\n" +

                "If you did not request a password reset, please ignore this email.\n\n" +

                "Regards,\n" +
                "Library Management"
        );

        mailSender.send(message);
    }
}