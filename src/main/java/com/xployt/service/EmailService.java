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
    
    public boolean sendPaymentNotification(String recipientEmail, String recipientName, double amount, String description, double newBalance) {
        String subject = "Payment Received - Xployt";
        
        String htmlContent = 
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h1 style='color: #4a4a4a;'>Payment Received</h1>" +
                "</div>" +
                "<p>Hello <strong>" + recipientName + "</strong>,</p>" +
                "<p>Great news! You have received a payment of <strong style='color: #28a745;'>$" + String.format("%.2f", amount) + "</strong>.</p>" +
                "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<p><strong>Payment Details:</strong></p>" +
                "<p>Description: " + description + "</p>" +
                "<p>Amount: $" + String.format("%.2f", amount) + "</p>" +
                "<p>New Balance: $" + String.format("%.2f", newBalance) + "</p>" +
                "</div>" +
                "<p>Thank you for your contribution to the Xployt platform!</p>" +
                "<p>Best regards,<br>The Xployt Team</p>" +
                "</div>";
        
        return sendHtmlEmail(recipientEmail, subject, htmlContent);
    }
} 