package kf.api;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kf.ListManger;
import kf.Parser;

public class ApiTest {
    File file = new File("src/test/java/kf/api/env.txt");
    private Api api = new Api(file);
    private ListManger listManger = new ListManger();
    private Parser parser = new Parser(new File("src/test/java/kf/api/Exemple.csv"));

    @Test
    @DisplayName("Test the sendInvoice method")
    void testSendInvoice() {

        Invoice invoice = new Invoice();
        invoice.setCustomerNumber("IID02234160");
        invoice.setInvoiceDate(LocalDate.now().toString());

        InvoiceRow invoiceRow = new InvoiceRow();
        invoiceRow.setArticleNumber("IO-AVG-2743");
        invoiceRow.setDeliveredQuantity(1);

    
        List<InvoiceRow> invoiceRows = new ArrayList<>();
        invoiceRows.add(invoiceRow);

    
        invoice.addInvoiceRows(invoiceRows);

     
        HttpResponse<String> response = api.sendInvoiceTest(invoice);
        assertEquals(201, response.statusCode());
        
        String documentNumber = getDocumentNumber(response);
        try {
            assertTrue(api.removeInvoice(documentNumber));
            System.out.println("Invoice removed");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        //Clean up 
        api.close();
    }

    
    @Test
    @DisplayName("Test sending several invoices")
    void testSendSeveralInvoices() {
       ArrayList<Invoice> invoices = parser.toInvoices(listManger.getExtraItems(), 
                                                    listManger.getForAll(), listManger.getDiscounts());

        ArrayList<String> documentNumbers = new ArrayList<>();
        String documentNumber = null;

        for(int i = 0; i < 10; i++){
            for (Invoice invoice : invoices) {
                HttpResponse<String> response = api.sendInvoiceTest(invoice);
                assertEquals(201, response.statusCode());
                documentNumber = getDocumentNumber(response);
                documentNumbers.add(documentNumber);
            }
        }
        
        for (String number : documentNumbers) {
            try {
                assertTrue(api.removeInvoice(number));
                System.out.println("Invoice removed");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        api.close();
        System.out.println("Test completed");
    }

    @Test
    @DisplayName("Test the removeInvoice method")
    void testRemoveInvoice(){
        int rateLimit = 25;
        int counter = 0;
        for (int i = 108; i < 120; i++ ){
            counter++;
            if (counter == rateLimit){
                try {
                    Thread.sleep(5000);
                    counter = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                assertTrue(api.removeInvoice(String.valueOf(i)));
                System.out.println("Invoice removed");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    

    private String getDocumentNumber(HttpResponse<String> response) {
        String responseBody = response.body();
        JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);

        // Extract the DocumentNumber
        String documentNumber = jsonResponse.getAsJsonObject("Invoice").get("DocumentNumber").getAsString();
        System.out.println("DocumentNumber: " + documentNumber);
        return documentNumber;
    }


}
