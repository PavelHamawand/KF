package kf;

import java.io.File;
import java.io.IOException;

import kf.api.Api;


public class Main {
    public static void main(String[] args) {
        
        Parser pars = new Parser(new File("ostersund/src/main/java/kf/example.csv"));
        Api api = new Api();
        try {
            System.out.println(api.sendInvoiceList(pars.toInvoices(InvoiceItem.testInvoiceItems())));
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending invoices");
            e.printStackTrace();
        }
        
    }
}