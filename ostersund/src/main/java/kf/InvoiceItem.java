package kf;

import java.util.ArrayList;

public class InvoiceItem { // dåligt namn icke plural
    public String key;
    public String articleNbr;
    public double price;

    
    public InvoiceItem (String key, String articleNbr, double price){
        this.key = key;
        this.articleNbr = articleNbr;
        this.price = price;

    }

    public static ArrayList<InvoiceItem> testInvoiceItems(){

        ArrayList<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
        invoiceItems.add(new InvoiceItem("Kajakplats", "IO-AVG-1478", 1450.0));
        invoiceItems.add(new InvoiceItem("Utökat träningskort", "IO-AVG-1479", 690.0));
        invoiceItems.add(new InvoiceItem("Medlemsavgift", "IO-AVG-2743", 105.0));
        invoiceItems.add(new InvoiceItem("Träningskort", "IO-AVG-43392", 880.0));
        return invoiceItems;
    }
}



