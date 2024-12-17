package kf.api;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Semaphore;
import java.io.IOException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;

/**
 * A local HTTP server implementation for handling OAuth 2.0 authorization code flow.
 * This server creates a temporary endpoint to receive the authorization callback
 * and extract the authorization code.
 * 
 * @author Berni
 * @version 1.0
 */
public class LocalServer {
    HttpServer server = null;
    private String authCode = null;
    private Semaphore signal = new Semaphore(0);
    private String authUrl = null;

    public LocalServer(String authUrl) {
        this.authUrl = authUrl;
    }


    /**
     * Initializes and starts a local HTTP server on port 8080.
     * Creates a context for handling OAuth callback requests at "/callback" endpoint.
     * 
     * @throws IOException if an error occurs while creating or starting the server
     */
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/callback", new AuthHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080/callback");
    }

    /**
     * Opens the default browser to the authorization URL and waits for the authorization code.
     * 
     * @return the authorization code received from the OAuth server
     */
    public String getAuthCode() {
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new java.net.URI(authUrl));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
        System.out.println("Waiting for authorization code...");
        try {
            signal.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return authCode;
    }

    /**
     * Stops the local HTTP server.
     */
    public void close() {
        if (server != null) {
            server.stop(0); // 0 means stop immediately
            System.out.println("Server stopped");
        }
    }

    /**
     * A handler for processing HTTP requests to the "/callback" endpoint.
     * Extracts the authorization code from the query parameters and sends a response to the browser.
     */
    private class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();

            // Extract authorization code from the query parameters
            if (query != null && query.contains("code=")) {
                authCode = query.split("code=")[1].split("&")[0];
                System.out.println("Authorization code received: " + authCode);
            }

            // Respond to the browser
            String response = "Authorization successful! You can close this window.";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            signal.release();
        }
    }
}