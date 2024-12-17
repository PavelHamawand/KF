package kf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kf.api.Api;
import kf.api.Invoice;
import kf.api.InvoiceRow;

public class FunctionalTest {

    private Parser parser;
    private ListManager listManger;
    private Api api;
    private final File TEST_CSV = new File("src/test/java/kf/Exemple.csv");
    private final File TEST_ENV = new File("src/test/java/kf/env.txt");

    @BeforeEach
    void setup() {
        assertTrue(TEST_CSV.exists(), "Testfilen 'Exemple.csv' saknas!");
        assertTrue(TEST_ENV.exists(), "Testfilen 'env.txt' saknas!");

        this.listManger = new ListManager();
        this.parser = new Parser(TEST_CSV);
        this.api = new Api(TEST_ENV);
    }

    @Test
    @DisplayName("TC1.1: Korrekt CSV-läsning")
    void testCorrectCsvInput() {    
        assertDoesNotThrow(() -> new Parser(TEST_CSV));
    }

    @Test
    @DisplayName("TC1.2: Ogiltig CSV-läsning")
    void testInvalidCsvInput() {
        File invalidFile = new File("ostersund\\src\\test\\java\\kf\\api\\FunctionalTest.java");
        assertThrows(IllegalArgumentException.class, () -> new Parser(invalidFile));
    }

    @Test
    @DisplayName("TC2.1: Fakturagenerering")
    void testInvoiceGeneration() {
        ArrayList<Invoice> invoices = parser.toInvoices(
                listManger.getExtraItems(), 
                listManger.getForAll(), 
                listManger.getDiscounts());
        
        assertNotNull(invoices);
        assertFalse(invoices.isEmpty(), "Fakturor ska genereras korrekt");
        
        // Verifiera fakturainnehåll
        Invoice firstInvoice = invoices.get(0);
        assertNotNull(firstInvoice.getCustomerNumber());
        assertNotNull(firstInvoice.getInvoiceDate());
        assertFalse(firstInvoice.getInvoiceRows().isEmpty());
    }

    @Test
    @DisplayName("TC3.1: Lägg till ny fakturapost")
    void testAddInvoiceItem() {
        String testArticle = "Ny Test Article";
        String testNumber = "TEST-" + System.currentTimeMillis();
        double testPrice = 150.0;
        
        listManger.addInvoiceItem(testArticle, testNumber, testPrice);
        
        boolean itemFound = listManger.getInvoiceItems().stream()
                .anyMatch(item -> item.key.equals(testArticle) && 
                                item.articleNbr.equals(testNumber) && 
                                item.price == testPrice);
        
        assertTrue(itemFound, "Ny artikel ska läggas till framgångsrikt");
    }

    @Test
    @DisplayName("TC3.2: Avvisa ogiltigt rabattpris")
    void testAddInvalidDiscount() {
        Exception e = assertThrows(IllegalArgumentException.class, 
            () -> listManger.addDiscount("Invalid Discount", "DISC-123", 200));
        assertEquals("Price must be negative", e.getMessage());
    }

    @Test
    @DisplayName("TC4.1: API: Skapa och skicka faktura")
    void testSendInvoice() throws IOException, InterruptedException {
        // Skapa testfaktura
        Invoice invoice = new Invoice();
        invoice.setCustomerNumber("IID02234160"); // Använd giltigt kundnummer
        invoice.setInvoiceDate(java.time.LocalDate.now().toString());

        InvoiceRow row = new InvoiceRow();
        row.setArticleNumber("IO-AVG-2743"); // Använd giltigt artikelnummer
        row.setDeliveredQuantity(1);

        invoice.addInvoiceRow(row);

        // Skicka faktura och verifiera
        boolean result = api.sendInvoice(invoice);
        assertTrue(result, "Fakturan ska skickas framgångsrikt");
    }

    @Test
    @DisplayName("TC6.1: API: Ta bort ogiltig faktura")
    void testRemoveInvalidInvoice() throws IOException, InterruptedException {
        boolean result = api.removeInvoice("INVALID-" + System.currentTimeMillis());
        assertFalse(result, "Borttagning av ogiltig faktura ska misslyckas");
    }

    @AfterEach 
    void tearDown() {
        if (api != null) {
            api.close();
        }
    }
}
