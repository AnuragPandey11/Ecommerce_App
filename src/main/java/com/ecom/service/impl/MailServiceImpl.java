package com.ecom.service.impl;

import com.ecom.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    // ✅ Replaces JavaMailSender with RestClient (HTTP API)
    private final RestClient restClient = RestClient.create();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    /**
     * Generic helper to send email via Brevo API
     */
    private void sendEmailViaApi(String to, String subject, String htmlContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        // Construct JSON payload matching Brevo API docs
        Map<String, Object> payload = Map.of(
            "sender", Map.of("email", fromEmail, "name", "Yap Team"),
            "to", List.of(Map.of("email", to)),
            "subject", subject,
            "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .uri(url)
                    .header("api-key", brevoApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("✅ Email sent via API to: {}", to);
        } catch (Exception ex) {
            log.error("❌ Failed to send email via API to {}: {}", to, ex.getMessage());
            // We log but don't throw, to prevent rolling back the User registration if email fails
            // (Optional: throw RuntimeException if you prefer strictly transactional behavior)
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = frontendBaseUrl + "/auth/verify-email?token=" + token;
        
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Welcome to Yap!</h2>
                <p>Please verify your email address to activate your account.</p>
                <p>
                    <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Verify Email
                    </a>
                </p>
                <p>Or click this link: <a href="%s">%s</a></p>
                <p><i>Link valid for 24 hours.</i></p>
            </body>
            </html>
            """, verifyUrl, verifyUrl, verifyUrl);

        sendEmailViaApi(toEmail, "🔐 Verify Your E-Commerce Account", htmlContent);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Reset Your Password</h2>
                <p>We received a request to reset your password.</p>
                <p>
                    <a href="%s" style="background-color: #008CBA; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Reset Password
                    </a>
                </p>
                <p><i>Link valid for 60 minutes.</i></p>
            </body>
            </html>
            """, resetUrl);

        sendEmailViaApi(toEmail, "🔐 Reset Your Password", htmlContent);
    }
}