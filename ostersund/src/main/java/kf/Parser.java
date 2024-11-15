package kf;
import kf.api.Invoice;
import kf.api.InvoiceRow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Parser {
    private ArrayList<String[]> data;
    private File file;
    private HashMap<String, Integer> header;

    public Parser(File file){
        this.data = new ArrayList<>();
        this.file = file;
        header = new HashMap<>();
        parse();
    }
    @SuppressWarnings("unused")
    private ArrayList<String[]> parse(){
        try (Scanner scan = new Scanner(file);) {
            for(int i = 0; scan.hasNextLine(); i++){ // for loop för att kunna ha någon error checking i csv filen och vet vilken rad det är. vetefan gör endå valideringen senare.
                String line = scan.nextLine();
                data.add(line.split(";")); 
            }
        } catch (FileNotFoundException e) {
            System.out.println("gissningsvis ingen fil");
            e.printStackTrace();
        }

        for(int i = 0 ; i < data.get(0).length; i++){ // snyggare sätt att leta efter rätt column
            header.put(data.get(0)[i], i);
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
        return data;
    }  

    public ArrayList<Invoice> toInvoices(ArrayList<InvoiceItems> items){
        if(header.get("Grupp/Lag/Arbetsrum/Familj") != null){ // får ha nån snyggare error hantering senare;
            return null;
        }
        ArrayList<Invoice> invoices = new ArrayList<>();
        for(int x = 1; x < data.size(); x++){ // börja på 1 för att skippa headern. går igenom alla kunder.
            Invoice tempInvoice = new Invoice();
            tempInvoice.setInvoiceRows(toRows(data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")].split(","), items));
            tempInvoice.setCustomerNumber(data.get(x)[header.get("IdrottsID")]);
            tempInvoice.setInvoiceDate("2024-11-11");
        }

        return invoices;
    }

    private ArrayList<InvoiceRow> toRows(String[] items, ArrayList<InvoiceItems> itemFilter){
        ArrayList<InvoiceRow> rows = new ArrayList<>();
        for(String item : items){
            for (InvoiceItems invoiceItems : itemFilter) {
                if(item.equals(invoiceItems.key)){
                    InvoiceRow tempInvoiceRow = new InvoiceRow();
                    tempInvoiceRow.setArticleNumber(invoiceItems.articleNbr);
                    tempInvoiceRow.setDeliveredQuantity(1);
                    tempInvoiceRow.setPrice(invoiceItems.price);
                }
            }
        }
        return rows;
    }

    public static void main(String[] args) {
        Parser pars = new Parser(new File("ostersund/src/main/java/kf/example.csv"));

    }
}