package com.prateek;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class ExtractNameFromEmail {
    public static void main(String[] args) {
        // Example email with display name
        String emailWithName = "hi.prateekrai@gmail.com";

        try {
            // Parse the email address using InternetAddress
            InternetAddress[] addresses = InternetAddress.parse(emailWithName);

            for (InternetAddress address : addresses) {
                // Get the personal (display) name
                String displayName = address.getPersonal();

                // If displayName is null, fallback to the email username part
                if (displayName == null || displayName.isEmpty()) {
                    // Extract the part before '@' from the email address
                    displayName = address.getAddress().split("@")[0];
                }

                System.out.println("Extracted Name: " + displayName);
            }
        } catch (AddressException e) {
            e.printStackTrace();
        }
    }
}