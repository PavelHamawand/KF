package kf.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Api{
    private String clientId;
    private String clientSecret;
    private String authCode;
    private String accessToken;
    private String refreshToken;


    public Api(){
        File file = new File("ostersund/src/main/java/kf/api/env.txt");
        try {
            Scanner scanner = new Scanner(file);
            clientId = scanner.nextLine();
            clientSecret = scanner.nextLine();
            scanner.close();
        } catch (FileNotFoundException e) {
            // Failar om env fil inte hittas eller om den inte innehåller clientId och clientSecret
            e.printStackTrace();
        }
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + clientSecret);
    }

    public void setAuthCode(String authCode){
        this.authCode = authCode;
    }

    public void getAccessToken(){
        System.out.println("Getting access token...");
        
    }


}