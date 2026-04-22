package com.musicmatch.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "mail.enabled", havingValue = "false", matchIfMissing = true)
public class MockEmailService implements EmailService {

    @Value("${app.auth.verify-email-url-base}")
    private String verifyEmailUrlBase;

    @Value("${app.auth.reset-password-url-base}")
    private String resetPasswordUrlBase;

    @Override
    public void sendVerificationEmail(String to, String username, String token) {
        String url = verifyEmailUrlBase + token;

        System.out.println("\n==== EMAIL VERIFICACIÓN ====");
        System.out.println("Usuario: " + username);
        System.out.println("Email: " + to);
        System.out.println("Link: " + url);
        System.out.println("===========================\n");
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String token) {
        String url = resetPasswordUrlBase + token;

        System.out.println("\n==== RESET PASSWORD ====");
        System.out.println("Usuario: " + username);
        System.out.println("Email: " + to);
        System.out.println("Link: " + url);
        System.out.println("=======================\n");
    }
}
