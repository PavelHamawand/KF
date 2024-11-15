package kf;

import java.io.IOException;
import kf.api.LocalServer;
import kf.api.Api;

public class Main {
    public static void main(String[] args) {

        Api api = new Api();
        LocalServer server = new LocalServer();
        try {
            server.startServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        api.setAuthCode(server.getAuthCode());
        api.getAccessToken();
        api.createTestInvoice();
    }
}