package com.ecom.service.impl;

import com.ecom.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("taskExecutor")
    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = frontendBaseUrl + "/verify-email?token=" + token;
        
        // For testing in Postman, you can use the API endpoint directly
        String apiVerifyUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🔐 Verify Your E-Commerce Account");
        message.setText(String.format("""
                Hello!
                
                Welcome to our E-Commerce platform! 
                
                Please verify your email address to activate your account and start shopping.
                
                🔗 Click here to verify:
                %s
                
                ⚠️ For testing purposes, you can also verify via API:
                %s
                
                This link is valid for 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                E-Commerce Team
                """, verifyUrl, apiVerifyUrl));

        try {
            mailSender.send(message);
            log.info("✅ Verification email sent successfully to: {}", toEmail);
        } catch (Exception ex) {
            log.error("❌ Failed to send verification email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send verification email", ex);
        }
    }
    @Override
public void sendPasswordResetEmail(String toEmail, String token) {
    String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject("🔐 Reset Your Password");
    message.setText(String.format("""
            Hello,
            
            We received a request to reset your password.
            
            🔗 Click here to reset your password:
            %s
            
            ⚠️ This link is valid for 60 minutes only.
            
            If you didn't request this, please ignore this email.
            
            Best regards,
            E-Commerce Team
            """, resetUrl));

    try {
        mailSender.send(message);
        log.info("✅ Password reset email sent successfully to: {}", toEmail);
    } catch (Exception ex) {
        log.error("❌ Failed to send password reset email to {}: {}", toEmail, ex.getMessage(), ex);
        throw new RuntimeException("Failed to send password reset email", ex);
    }
}

}
