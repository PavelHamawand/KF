package kf.api;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class Api {
    private String clientId;
    private String clientSecret;
    private String authCode;
    private String authURL;
    private String accessToken;
    private String redirectURL;
    HttpClient client;

    public Api() {
        File file = new File("ostersund/src/main/java/kf/api/env.txt");
        this.client = HttpClient.newHttpClient();

        try {
            Scanner scanner = new Scanner(file);
            clientId = scanner.nextLine();
            clientSecret = scanner.nextLine();
            authURL = scanner.nextLine();
            redirectURL = scanner.nextLine();
            scanner.close();
        } catch (FileNotFoundException e) {
            // Failar om env fil inte hittas eller om den inte innehåller clientId och
            e.printStackTrace();
        }
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + clientSecret);
        System.out.println("Please visit Auth URL: " + authURL);

    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void getAccessToken() {
        System.out.println("Getting access token...");
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        // Request header+body
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apps.fortnox.se/oauth-v1/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + credentials)
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&code=" + authCode
                        + "&redirect_uri=" + redirectURL))
                .build();

        try {
            // Extract access token and refresh token from response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body().toString();

            // Find the index of "access_token" in the response string
            int tokenIndex = responseBody.indexOf("\"access_token\"");

            // If "access_token" is found, extract the value
            if (tokenIndex != -1) {
                int startIndex = responseBody.indexOf("\"", tokenIndex + 14) + 1;
                int endIndex = responseBody.indexOf("\"", startIndex);
                accessToken = responseBody.substring(startIndex, endIndex);
            }

            if (accessToken != null) {
                System.out.println("Access Token: " + accessToken);
            } else {
                System.out.println("Access token not found in response.");
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void createTestInvoice() {
        if (accessToken == null) {
            System.out.println("Access token is missing. Please authenticate first.");
            return;
        }

        System.out.println("Creating test invoice...");

        // Create invoice JSON
        String invoiceJson = "{ \"Invoice\": { \"CustomerNumber\": \"1\", \"InvoiceDate\": \"2023-10-01\", \"DueDate\": \"2023-10-15\", \"Total\": \"1000\" } }";


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.fortnox.se/3/invoices")) // Replace with the correct endpoint
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(invoiceJson))
                .build();

        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode == 201 || responseCode == 200) {
                System.out.println("Test invoice created successfully!");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("Failed to create invoice. HTTP Response Code: " + responseCode);
                System.out.println("Response: " + response.body());
            }
        }
    catch(IOException| InterruptedException e)
        {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

    
    