package com.prateek.lld;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SendEmail {
    public static void main(String[] args) {
        // Sender's email ID and password (your Gmail account)
        final String username = "prateekrai2699@gmail.com"; // Change to your email
        final String password = "iheg taiw qwvv vdei"; // Change to your password

        // Receiver's email ID
        String to = "hi.prateekrai@gmail.com"; // Change to the recipient's email

        // Setting up the Gmail SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Get the Session object
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            String messageBody = Files.readString(Paths.get("src/main/java/com/prateek/lld/message.txt"));
            // Create a default MimeMessage object
            Message message = new MimeMessage(session);

            // Set the From field
            message.setFrom(new InternetAddress(username , "Prateek Rai"));

            // Set the To field
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Set the Subject field
            message.setSubject("Interested in Java Developer Role With 3+ Years of Experience  ");

            // Set the content of the email
            message.setText(messageBody);

            message.setHeader("X-Priority", "1"); // 1 = High, 3 = Normal, 5 = Low
            message.setHeader("Importance", "High"); // Optional, for Microsoft clients
            message.setHeader("Priority", "Urgent");
            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
