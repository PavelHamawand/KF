package kf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    private File file;

    public Parser(File file){
        this.file = file;

    }

    public void parse(){
        ArrayList<String[]> data = new ArrayList<>();
        try (Scanner scan = new Scanner(file);) {
            for(int i = 0; scan.hasNextLine(); i++){ // for loop för att kunna ha någon error checking i csv filen och vet vilken rad det är. vetefan gör endå valideringen senare.
                String line = scan.nextLine();
                data.add(line.split(";")); 
            }
        } catch (FileNotFoundException e) {
            System.out.println("gissningsvis ingen fil");
            e.printStackTrace();
        }


        int setlenght = data.get(0).length; // validerar att alla rader har är lika långa.
        for(int i = 1; i < data.size(); i ++){
            if(data.get(i).length != setlenght){
                System.err.println("nåt konstigt vid rad " + i);
            }
        }

        for(int i = 1; i < data.size(); i++){
            for(int k = 0; k < data.get(i).length; k++){
                System.out.print(data.get(0)[k] + " = " + data.get(i)[k] + "   ");
            }
            System.out.print("\n");
        }
    }  




    public static void main(String[] args) {
        Parser pars = new Parser(new File("ostersund/src/main/java/kf/example.csv"));
        pars.parse();
    }
}