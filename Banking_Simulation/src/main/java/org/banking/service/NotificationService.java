package org.banking.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class NotificationService {

    private static String SMTP_HOST;
    private static String SMTP_PORT;
    private static String SMTP_USERNAME;
    private static String SMTP_PASSWORD;
    private static String FROM_EMAIL;
    private static String FROM_NAME;
    private static boolean SMTP_AUTH;
    private static boolean SMTP_STARTTLS;

    static {
        try (InputStream input = NotificationService.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            SMTP_HOST = prop.getProperty("mail.smtp.host");
            SMTP_PORT = prop.getProperty("mail.smtp.port");
            SMTP_USERNAME = prop.getProperty("mail.username");
            SMTP_PASSWORD = prop.getProperty("mail.password");
            FROM_EMAIL = prop.getProperty("mail.from.email");
            FROM_NAME = prop.getProperty("mail.from.name");
            SMTP_AUTH = Boolean.parseBoolean(prop.getProperty("mail.smtp.auth"));
            SMTP_STARTTLS = Boolean.parseBoolean(prop.getProperty("mail.smtp.starttls.enable"));

        } catch (Exception e) {
            System.err.println("Failed to load email configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send notification email
     */
    public void sendNotification(String toEmail, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", String.valueOf(SMTP_AUTH));
            props.put("mail.smtp.starttls.enable", String.valueOf(SMTP_STARTTLS));

            // Create session with authenticator
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send transaction success email alert to sender
     */
    public void sendEmailAlertToSender(String senderEmail, String senderName,
                                       String receiverName, String receiverAccountNumber,
                                       double amount, String transactionId,
                                       LocalDateTime transactionTime, String transactionMode,String description) {

        String subject = "Transaction Successful - Money Debited";
        String body = generateSenderEmailTemplate(senderName, receiverName, receiverAccountNumber,
                amount, transactionId, transactionTime, transactionMode,description);

        sendNotification(senderEmail, subject, body);
    }

    /**
     * Send transaction success email alert to receiver
     */
    public void sendEmailAlertToReceiver(String receiverEmail, String receiverName,
                                         String senderName, String senderAccountNumber,
                                         double amount, String transactionId,
                                         LocalDateTime transactionTime, String transactionMode, String description) {

        String subject = "Transaction Successful - Money Credited";
        String body = generateReceiverEmailTemplate(receiverName, senderName, senderAccountNumber,
                amount, transactionId, transactionTime, transactionMode, description);

        sendNotification(receiverEmail, subject, body);
    }

    /**
     * Generate sender email template
     */
    private String generateSenderEmailTemplate(String senderName, String receiverName,
                                               String receiverAccountNumber, double amount,
                                               String transactionId, LocalDateTime transactionTime,
                                               String transactionMode, String description) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
        String formattedTime = transactionTime.format(formatter);

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .header { background-color: rgb(16, 134, 231); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .transaction-details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e0e0e0; }
                .label { font-weight: bold; color: #555; }
                .value { color: #333; }
                .amount { font-size: 24px; color: #d32f2f; font-weight: bold; text-align: center; margin: 20px 0; }
                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e0e0e0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>💸 Money Debited</h2>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your transaction has been processed successfully. Money has been debited from your account.</p>
                    
                    <div class="amount">- ₹ %.2f</div>
                    
                    <div class="transaction-details">
                        <div class="detail-row">
                            <span class="label">Transaction ID:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Date & Time:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Transaction Mode:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Receiver Name:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Receiver Account:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Description:</span>
                            <span class="value">%s</span>
                        </div>
                    </div>
                    
                    <p style="color: #888; font-size: 14px;">If you did not authorize this transaction, please contact customer support immediately.</p>
                </div>
                <div class="footer">
                    <p>This is an automated email. Please do not reply.</p>
                    <p>&copy; 2025 Banking Simulation System. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(senderName, amount, transactionId, formattedTime, transactionMode,
                receiverName, receiverAccountNumber, description);
    }

    /**
     * Generate receiver email template
     */
    private String generateReceiverEmailTemplate(String receiverName, String senderName,
                                                 String senderAccountNumber, double amount,
                                                 String transactionId, LocalDateTime transactionTime,
                                                 String transactionMode, String description) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
        String formattedTime = transactionTime.format(formatter);

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                .header { background-color: rgb(16, 134, 231); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .transaction-details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e0e0e0; }
                .label { font-weight: bold; color: #555; }
                .value { color: #333; }
                .amount { font-size: 24px; color: #4caf50; font-weight: bold; text-align: center; margin: 20px 0; }
                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e0e0e0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>💰 Money Credited</h2>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your account has been credited with money. Transaction completed successfully.</p>
                    
                    <div class="amount">+ ₹ %.2f</div>
                    
                    <div class="transaction-details">
                        <div class="detail-row">
                            <span class="label">Transaction ID:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Date & Time:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Transaction Mode:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Sender Name:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Sender Account:</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="label">Description:</span>
                            <span class="value">%s</span>
                        </div>
                    </div>
                    
                    <p style="color: #888; font-size: 14px;">Thank you for banking with us!</p>
                </div>
                <div class="footer">
                    <p>This is an automated email. Please do not reply.</p>
                    <p>&copy; 2025 Banking Simulation System. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(receiverName, amount, transactionId, formattedTime, transactionMode,
                senderName, senderAccountNumber, description);
    }
}