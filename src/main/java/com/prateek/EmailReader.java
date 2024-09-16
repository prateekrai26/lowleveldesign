package com.prateek;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailReader {

    private static final String API_KEY = "your_api_key_here";
    private static final String API_URL = "https://api.hunter.io/v2/email-finder?domain=%s&api_key=%s";

    public static void main(String[] args) {
        String email = "example@example.com"; // Replace with the email you want to lookup
        try {
            String response = getEmailDetails(email);
            System.out.println("Response from API:");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getEmailDetails(String email) throws Exception {
        // The API endpoint and parameters will vary by service
        String urlString = String.format(API_URL, email, API_KEY);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}