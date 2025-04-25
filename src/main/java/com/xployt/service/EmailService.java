package com.xployt.service;

import java.util.logging.Logger;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import com.xployt.util.CustomLogger;

import io.github.cdimascio.dotenv.Dotenv;

public class EmailService {
    private static final Logger logger = CustomLogger.getLogger();
    private static final String EMAIL_FROM = "lakshith.k.nishshanke@gmail.com";
    private final Mailer mailer;
    private final String gmailPassword;

    public EmailService() {
        Dotenv dotenv = Dotenv.load();
        gmailPassword = dotenv.get("GMAIL_APP_PASSWORD");
        
        mailer = MailerBuilder
                .withSMTPServer("smtp.gmail.com", 587, EMAIL_FROM, gmailPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
    }

    public boolean sendEmail(String recipient, String subject, String content) {
        try {
            Email email = EmailBuilder.startingBlank()
                    .from(EMAIL_FROM)
                    .to(recipient)
                    .withSubject(subject)
                    .withPlainText(content)
                    .buildEmail();

            mailer.sendMail(email);
            
            logger.info("Email sent successfully to: " + recipient);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to send email: " + e.getMessage());
            return false;
        }
    }
    
    public boolean sendHtmlEmail(String recipient, String subject, String htmlContent) {
        try {
            Email email = EmailBuilder.startingBlank()
                    .from(EMAIL_FROM)
                    .to(recipient)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();

            mailer.sendMail(email);
            
            logger.info("HTML email sent successfully to: " + recipient);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to send HTML email: " + e.getMessage());
            return false;
        }
    }
} 