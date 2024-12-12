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
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class Api {
    private String clientId;
    private String clientSecret;
    private String authCode;
    private String authURL;
    private String accessToken;
    private String redirectURL;
    private HttpClient client;

    public Api(File file) {
        // Constructor able to change the env file for testing purposes 
        setup(file);

    }

    public Api() {
        File file = new File("ostersund/src/main/java/kf/api/env.txt");
        setup(file);
    }

    private void setup(File file) {
        try {
            Scanner scanner = new Scanner(file);
            clientId = scanner.nextLine();
            clientSecret = scanner.nextLine();
            authURL = scanner.nextLine();
            redirectURL = scanner.nextLine();
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Please create a file named 'env.txt' in the 'api' package.");
            enviorment();
            e.printStackTrace();
            System.exit(1);
        }

        // Temorary Server for authentication
        LocalServer server = new LocalServer(authURL);
        try {
            server.startServer();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HttpClient for sending requests
        this.client = HttpClient.newHttpClient();
        // temp for auth code
        System.out.println("Please visit Auth URL: " + authURL);
        this.authCode = server.getAuthCode();
        this.getAccessToken();
    }

    private void enviorment() {
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + clientSecret);
        System.out.println("Please visit Auth URL: " + authURL);
    }

    // Method to get access token
    private void getAccessToken() {
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

    // metod för att skicka en faktura returnerar true för genomförd och false för
    // misslyckad
    public boolean sendInvoice(Invoice invoice) throws IOException, InterruptedException {
        if (accessToken == null) {
            System.out.println("Access token is missing. Please authenticate first.");
            return false;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject invoiceJsonObject = new JsonObject();

        invoiceJsonObject.add("Invoice", gson.toJsonTree(invoice));

        // Convert the JsonObject to a String
        String invoiceJson = gson.toJson(invoiceJsonObject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.fortnox.se/3/invoices"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(invoiceJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int responseCode = response.statusCode();
        if (responseCode == 201) {
            return true;
        } else if (responseCode == 400) {
            System.out.println("Failed to create invoice. HTTP Response Code: " + responseCode);
            System.out.println("Response: " + response.body());
            return false;
        } else
            throw new IOException("Failed to create invoice. HTTP Response Code: " + responseCode);
    }

    // Skickar alla fakturor i en lista och returnerar antalet fakturor som
    // skickades
    public int sendInvoiceList(List<Invoice> invoices) throws IOException, InterruptedException {
        int sentInvoices = 0;
        if (accessToken == null) {
            System.out.println("Access token is missing. Please authenticate first.");
            return -1;
        }

        // Serialize to JSON using Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject invoiceJsonObject = new JsonObject();

        int rateLimit = 25;
        int counter = 0;

        for (Invoice invoice : invoices) {
            // Create a JsonObject and add the Invoice object under the "Invoice" key
            // Eventuellt lägg till wait här för att inte orsaka för många requests
            invoiceJsonObject.add("Invoice", gson.toJsonTree(invoice));

            // Convert the JsonObject to a String
            String invoiceJson = gson.toJson(invoiceJsonObject);

        
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.fortnox.se/3/invoices"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(invoiceJson))
                    .build();

            counter++;
            if (counter == rateLimit) {
                Thread.sleep(5000);
                counter = 0;
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode == 201) {
                sentInvoices++;
            } else
                throw new IOException("Failed to create invoice. HTTP Response Code: " + responseCode + "\n" + response.body()
                       + "\nFailed to send invoices. Only " + sentInvoices + " out of " + invoices.size()
                        + " invoices were sent.");
        }
        return sentInvoices;
    }

    public HttpResponse<String> sendInvoiceTest(Invoice invoice) {
        if (accessToken == null) {
            System.out.println("Access token is missing. Please authenticate first.");
            return null;
        }

        System.out.println("Creating test invoice...");
        // Serialize to JSON using Gson
        Gson gson = new Gson();

        // Create a JsonObject and add the Invoice object under the "Invoice" key
        JsonObject invoiceJsonObject = new JsonObject();
        invoiceJsonObject.add("Invoice", gson.toJsonTree(invoice));

        // Convert the JsonObject to a String
        String invoiceJson = gson.toJson(invoiceJsonObject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.fortnox.se/3/invoices"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(invoiceJson))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            if (responseCode == 201 || responseCode == 200) {
                System.out.println("Test invoice created successfully!");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("Failed to create invoice. HTTP Response Code: " + responseCode);
                System.out.println("Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
       
        return response;
    }

    public boolean removeInvoice(String invoiceNumber) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.fortnox.se/3/invoices/" + invoiceNumber + "/cancel"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> respone = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(respone.statusCode() == 200) {
            return true;
        } else {
            System.out.println("Failed to remove invoice. HTTP Response Code: " + respone.statusCode());
            System.out.println("Response: " + respone.body());
            return false;
        }
    }
}