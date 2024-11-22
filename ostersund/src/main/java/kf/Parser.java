package kf;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import kf.api.Invoice;
import kf.api.InvoiceRow;

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

    public ArrayList<Invoice> toInvoices(ArrayList<InvoiceItem> items, int validTime) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        
        for (int x = 1; x < data.size(); x++) { // börja på 1 för att skippa headern. går igenom alla kunder.
            Invoice tempInvoice = new Invoice();
            tempInvoice.setCustomerNumber(getColumnValue( x, "IdrottsID"));
            tempInvoice.setInvoiceRows(toRows(data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")].split(","), items));
            tempInvoice.setInvoiceDate(LocalDate.now().plusDays(validTime).toString());
            invoices.add(tempInvoice);
        }
        return invoices;
    }

    private String getColumnValue(int index, String kategori){
       if(data.get(index)[header.get(kategori)] == null){
            System.out.println("A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
            throw new IllegalArgumentException( "A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
       }
         return data.get(index)[header.get(kategori)];
    }

    private ArrayList<InvoiceRow> toRows(String[] items, ArrayList<InvoiceItem> itemFilter){
        ArrayList<InvoiceRow> rows = new ArrayList<>();
        for(String item : items){
            for (InvoiceItem invoiceItems : itemFilter) {
                if(item.contains(invoiceItems.key)){
                    InvoiceRow tempInvoiceRow = new InvoiceRow();
                    tempInvoiceRow.setArticleNumber(invoiceItems.articleNbr);
                    tempInvoiceRow.setDeliveredQuantity(1);
                    tempInvoiceRow.setPrice(invoiceItems.price);
                    rows.add(tempInvoiceRow);
                }
            }
        }
        return rows;
    }
}