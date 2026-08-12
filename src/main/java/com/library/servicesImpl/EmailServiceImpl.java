package com.library.servicesImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.library.services.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    // Empty default so the app does NOT crash on startup (local dev)
    // when MAIL_USERNAME / MAIL_PASSWORD env vars are not set.
    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    /**
     * Sends the OTP email asynchronously so the caller (forgotPassword API)
     * does not wait for slow SMTP round-trips and responds fast.
     * Any failure is logged with full detail here instead of being
     * swallowed as a generic error upstream.
     */
    @Override
    @Async
    public void sendOtpEmail(String toEmail, String userName, String otp) {

        if (fromEmail == null || fromEmail.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            log.error("EMAIL NOT SENT: MAIL_USERNAME / MAIL_PASSWORD is not configured. " +
                    "Set these environment variables (MAIL_USERNAME, MAIL_PASSWORD) on the server.");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Library Management - Password Reset OTP");

            String name = (userName != null && !userName.trim().isEmpty()) ? userName : "User";

            String body =
                    "Hello " + name + ",\n\n" +
                    "We received a request to reset your Library Management account password.\n\n" +
                    "Your OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 5 minutes.\n" +
                    "Do not share this OTP with anyone.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Regards,\n" +
                    "Library Management";

            helper.setText(body, false);

            long start = System.currentTimeMillis();
            mailSender.send(mimeMessage);
            long took = System.currentTimeMillis() - start;

            log.info("OTP email sent successfully to {} in {} ms", toEmail, took);

        } catch (Exception e) {
            // Full detail goes to Render logs so the real cause (auth failure,
            // timeout, wrong credentials, etc.) is always visible.
            log.error("FAILED to send OTP email to {}. Reason: {}", toEmail, e.getMessage(), e);
        }
    }
}