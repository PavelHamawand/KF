package kf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    private File file;
    private String[] reson;
    public Parser(File file, String[] reasons){
        this.file = file;
        this.reson = reasons;
    }

    public void parse(){
        ArrayList<String[]> data = new ArrayList<>();
        try {
            Scanner scan = new Scanner(file);

            for(int i = 0; scan.hasNextLine(); i++){ // for loop för att kunna ha någon error checking i csv filen och vet vilken rad det är.
                String line = scan.nextLine();
                data.add(line.split(";")); 
            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("gissningsvis ingen fil");
            e.printStackTrace();
        }

        for(int i = 0; i < data.get(0).length; i++){
            
        }

        for(int i = 1; i < data.size(); i++){
            for(int k = 1; k > data.get(i).length; i++){

            }
        }
    }  




    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}