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
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * The Api class handles authentication and communication with the Fortnox API.
 * It provides functionality for sending invoices and managing authentication tokens.
 * 
 * This class requires an env.txt file containing:
 * - Client ID (line 1)
 * - Client Secret (line 2) 
 * - Auth URL (line 3)
 * - Redirect URL (line 4)
 *
 * The class manages:
 * - OAuth 2.0 authentication flow
 * - Access token retrieval and management
 * - Invoice creation and cancellation
 * - Batch processing of invoices with rate limiting
 *
 * Important notes:
 * - The class automatically starts a temporary local server for authentication
 * - Rate limiting is implemented (25 requests per 5 seconds) for batch operations
 * - Failed batch operations will automatically cancel all successfully created invoices
 *
 * @author Your Name
 * @version 1.0
 * @see Invoice
 * @see LocalServer
 */
public class Api {
    private String clientId;
    private String clientSecret;
    private String authCode;
    private String authURL;
    private String accessToken;
    private String redirectURL;
    private HttpClient client;
    private LocalServer server;


       /**
     * Constructs an Api instance with the defualt configuration file.
     * 
     * 
     */
    public Api() {
        File file = new File("ostersund/src/main/java/kf/api/env.txt");
        setup(file);
    }

    /**
     * Constructs an Api instance with a specified configuration file.
     * 
     * @param file The environment configuration file to be used for setting up the API
     *             This allows for different configurations during testing.
     */
    public Api(File file) {
        // Constructor able to change the env file for testing purposes 
        setup(file);

    }

    /**
     * Sets up the API configuration by reading credentials from a file and initializing authentication.
     * 
     * This method performs the following steps:
     * 1. Reads client credentials and URLs from the specified file
     * 2. Starts a temporary local server for authentication
     * 3. Initializes HTTP client
     * 4. Gets authentication code through local server
     * 5. Retrieves access token
     * 
     * @param file The file containing API credentials and configuration (expected format: clientId, clientSecret, authURL, redirectURL on separate lines)
     * @throws FileNotFoundException if the specified configuration file is not found
     * @throws IOException if the local server fails to start
     */
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
        this.server = new LocalServer(authURL);
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

    /**
     * Prints environment-related information to the console.
     * This method displays the client ID, client secret, and authentication URL
     * for debugging or verification purposes.
     */
    private void enviorment() {
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + clientSecret);
        System.out.println("Please visit Auth URL: " + authURL);
    }

    /**
     * Retrieves the access token from the Fortnox API using the client credentials and authentication code.
     * 
     * This method sends a POST request to the Fortnox API with the client credentials and authentication code.
     * The response contains the access token and refresh token, which are extracted and stored for future requests.
     */
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

    /**
     * Sends an invoice to the Fortnox API using the provided Invoice object.
     * 
     * This method serializes the Invoice object to JSON and sends a POST request to the Fortnox API.
     * The response is checked for success and the method returns a boolean indicating the result.
     * 
     * @param invoice The Invoice object to be sent to the Fortnox API
     * @return true if the invoice was successfully sent, false otherwise
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
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

    /**
     * Sends a list of invoices to the Fortnox API using the provided List of Invoice objects.
     * 
     * This method serializes each Invoice object to JSON and sends a POST request to the Fortnox API.
     * The response is checked for success and the method returns the number of invoices successfully sent.
     * 
     * @param invoices The List of Invoice objects to be sent to the Fortnox API
     * @return the number of invoices successfully sent
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    public int sendInvoiceList(List<Invoice> invoices) throws IOException, InterruptedException {
        int sentInvoices = 0;
        int rateLimit = 25;
        int counter = 0;
        HashSet<String> documentNumbers = new HashSet<String>();


        if (accessToken == null) {
            System.out.println("Access token is missing. Please authenticate first.");
            return -1;
        }

        // Serialize to JSON using Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject invoiceJsonObject = new JsonObject();

       

        for (Invoice invoice : invoices) {
            // Create a JsonObject and add the Invoice object under the "Invoice" key
            invoiceJsonObject.add("Invoice", gson.toJsonTree(invoice));

            // Convert the JsonObject to a String
            String invoiceJson = gson.toJson(invoiceJsonObject);

        
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.fortnox.se/3/invoices"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(invoiceJson))
                    .build();

            // Rate limit handling
            
            if (counter == rateLimit) {
                Thread.sleep(5000);
                counter = 0;
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();

            if (responseCode == 201) {
                
                counter++;
                sentInvoices++;
                documentNumbers.add(getDocumentNumber(response));
                
            } else {
                for(String number : documentNumbers){
                    removeInvoice(number);
                }
               throw new IOException("Failed to create invoice. HTTP Response Code: " + responseCode + "\n" + response.body()
                      + "\nFailed to send invoices. Only " + sentInvoices + " out of " + invoices.size()
                       + " invoices were sent." + "\n All invoices have been removed" );
            }
                 
        }
        return sentInvoices;
    }

    /**
     * Sends a test invoice to the Fortnox API using the provided Invoice object.
     * 
     * This method serializes the Invoice object to JSON and sends a POST request to the Fortnox API.
     * The response is checked for success and the method returns the HttpResponse object.
     * 
     * @param invoice The Invoice object to be sent to the Fortnox API
     * @return the HttpResponse object containing the response from the Fortnox API
     */
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

    /**
     * Removes an invoice from the Fortnox API using the provided invoice number.
     * 
     * This method sends a PUT request to the Fortnox API to cancel the invoice with the specified number.
     * The response is checked for success and the method returns a boolean indicating the result.
     * 
     * @param invoiceNumber The invoice number of the invoice to be removed
     * @return true if the invoice was successfully removed, false otherwise
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
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

    private String getDocumentNumber(HttpResponse<String> response) {
        String responseBody = response.body();
        JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);

        // Extract the DocumentNumber
        String documentNumber = jsonResponse.getAsJsonObject("Invoice").get("DocumentNumber").getAsString();
        System.out.println("DocumentNumber: " + documentNumber);
        return documentNumber;
    }

    /**
     * Closes the local server used for authentication.
     */
    public void close() {
        server.close();
    }

    
    }
