package kf.api;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.IOException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;

public class LocalServer  {
    private static String authCode = null;

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/callback", new AuthHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080/callback");
    }

    public synchronized String getAuthCode() {
        while(authCode == null){
          try {
              wait();
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              e.printStackTrace();
          }
        }
        return authCode;
    }

    static class AuthHandler  implements HttpHandler {
        @Override
        public synchronized void handle(HttpExchange exchange) throws IOException {
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
            notifyAll();
        }
    }
}