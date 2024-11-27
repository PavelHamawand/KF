package kf;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private String getColumnValue(int index, String kategori){
        if(data.get(index)[header.get(kategori)] == null){
             System.out.println("A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
             throw new IllegalArgumentException( "A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
        }
          return data.get(index)[header.get(kategori)];
     }

    public ArrayList<Invoice> toInvoices(ArrayList<InvoiceItem> extraItems,
        ArrayList<InvoiceItem> forAllList, 
        ArrayList<InvoiceItem> discountList) 
        throws IllegalArgumentException{ 
        ArrayList<Invoice> invoices = new ArrayList<>();
      
        List<InvoiceRow> forAllRows  = forAll(forAllList);
        
        for (int x = 1; x < data.size(); x++) { // börja på 1 för att skippa headern. går igenom alla kunder.
            Invoice tempInvoice = new Invoice();
            tempInvoice.setCustomerName(getColumnValue(x, "Förnamn") + " " + getColumnValue(x, "Efternamn"));
            tempInvoice.setCustomerNumber(getColumnValue( x, "IdrottsID"));
            
            // Lägger till standard "For all" artiklar
            tempInvoice.addInvoiceRows(forAllRows);
            

            //Om det finns extra tjänster
            if(data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")] != null){
                tempInvoice.addInvoiceRows(toRows(data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")].split(","), extraItems));
            }
            
            //Om det finns rabatt
            if(data.get(x)[header.get("Rabatt")] != null){
                tempInvoice.addInvoiceRow(discount(data.get(x)[header.get("Rabatt")], discountList));
            }

            tempInvoice.setInvoiceDate(LocalDate.now().toString());
            invoices.add(tempInvoice);
        }
        return invoices;
    }

    //gör en invoiceRow med de items som är för alla, ska denna ha mer error checking?
    private List<InvoiceRow> forAll(ArrayList<InvoiceItem> forAll) throws IllegalArgumentException{
        List<InvoiceRow> invoiceRows = new ArrayList<>();
        double rabatt = 0.8; // byt ut just nu 20% rabatt 
        if(forAll == null){
            throw new IllegalArgumentException("Det finns inga items för alla");
        }
        try {
            for (InvoiceItem item : forAll) {
                if (item.forAll) {
                    InvoiceRow row = new InvoiceRow();
                    row.setArticleName(item.key);
                    row.setArticleNumber(item.articleNbr);
                    row.setDeliveredQuantity(1); // ska denna alltid vara 1? varför inte hårdkoda isåfall?
                    row.setPrice(item.price * rabatt);
                    invoiceRows.add(row);
                }
            }
        } catch (Exception e) {
            System.out.println("Något gick fel vid skapandet av rabatterna för alla");
            e.printStackTrace();
        }
        
        return invoiceRows;
    }
    
    private ArrayList<InvoiceRow> toRows(String[] items, ArrayList<InvoiceItem> itemFilter){
        ArrayList<InvoiceRow> rows = new ArrayList<>();
        for(String item : items){
            for (InvoiceItem invoiceItems : itemFilter) {
                if(item.contains(invoiceItems.key)){
                    InvoiceRow tempInvoiceRow = new InvoiceRow();
                    tempInvoiceRow.setArticleName(invoiceItems.key);
                    tempInvoiceRow.setArticleNumber(invoiceItems.articleNbr);
                    tempInvoiceRow.setDeliveredQuantity(1);
                    tempInvoiceRow.setPrice(invoiceItems.price);
                    rows.add(tempInvoiceRow);
                }
            }
        }
        return rows;
    }

    private InvoiceRow discount(String discount, ArrayList<InvoiceItem> discountList){
        for (InvoiceItem invoiceItems : discountList) {
            if(discount.contains(invoiceItems.key)){
                InvoiceRow tempInvoiceRow = new InvoiceRow();
                tempInvoiceRow.setArticleName(invoiceItems.key);
                tempInvoiceRow.setArticleNumber(invoiceItems.articleNbr);
                tempInvoiceRow.setDeliveredQuantity(1);
                tempInvoiceRow.setPrice(invoiceItems.price);
                return tempInvoiceRow;
            }
        }
        return null;
    }

}