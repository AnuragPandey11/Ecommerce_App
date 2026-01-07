package com.ecom.service;

public interface MailService {
    
    void sendVerificationEmail(String toEmail, String token);
    
    void sendPasswordResetEmail(String toEmail, String token);
}
