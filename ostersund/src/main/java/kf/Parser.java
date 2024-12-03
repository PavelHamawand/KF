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

    public Parser(File file) {
        this.data = new ArrayList<>();
        this.file = file;
        header = new HashMap<>();
        parse();
    }

    @SuppressWarnings("unused")
    private ArrayList<String[]> parse() {
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                data.add(parseLine(line));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }

        for (int i = 0; i < data.get(0).length; i++) { // snyggare sätt att leta efter rätt column
            header.put(data.get(0)[i], i);
        }

        int setlenght = data.get(0).length; // validerar att alla rader har är lika långa.
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).length != setlenght) {
                System.err.println("nåt konstigt vid rad " + i);
            }
        }

        for (int i = 1; i < data.size(); i++) {
            for (int k = 0; k < data.get(i).length; k++) {
                System.out.print(data.get(0)[k] + " = " + data.get(i)[k] + "   ");
            }
            System.out.print("\n");
        }
        return data;
    }

    private String[] parseLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
    
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes; // Toggle the inQuotes flag
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0); // Reset the StringBuilder
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim()); // Add the last field
    
        return fields.toArray(new String[0]);
    }

    private String getColumnValue(int index, String kategori) {
        if (data.get(index)[header.get(kategori)] == null) {
            System.out.println("A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
            throw new IllegalArgumentException(
                    "A value for" + kategori + "in row" + index + " does not exist in the CSV file.");
        }
        return data.get(index)[header.get(kategori)];
    }

    public ArrayList<Invoice> toInvoices(ArrayList<InvoiceItem> extraItems,
            ArrayList<InvoiceItem> forAllList,
            ArrayList<InvoiceItem> discountList)
            throws IllegalArgumentException {
        ArrayList<Invoice> invoices = new ArrayList<>();

        List<InvoiceRow> forAllRows = forAll(forAllList);

        for (int x = 1; x < data.size(); x++) { // börja på 1 för att skippa headern. går igenom alla kunder.
            Invoice tempInvoice = new Invoice();
            tempInvoice.setCustomerName(getColumnValue(x, "Förnamn") + " " + getColumnValue(x, "Efternamn"));
            tempInvoice.setCustomerNumber(getColumnValue(x, "IdrottsID"));

            // Lägger till standard "For all" artiklar
            tempInvoice.addInvoiceRows(forAllRows);

            // Om det finns extra tjänster
            try {
                if (data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")] != null) {
                    tempInvoice.addInvoiceRows(
                            toRows(data.get(x)[header.get("Grupp/Lag/Arbetsrum/Familj")].split(","), extraItems));
                }
            } catch (Exception e) {
                System.out.println("Något gick fel vid skapandet av extra tjänster");
                e.printStackTrace();
            }

            // Om det finns rabatt
            try {
                if (data.get(x)[header.get("Rabatt")] != "") {
                    tempInvoice.addInvoiceRow(discount(data.get(x)[header.get("Rabatt")], discountList));
                }
            } catch (Exception e) {
                System.out.println("Något gick fel vid skapandet av rabatten");
            }

            tempInvoice.setInvoiceDate(LocalDate.now().toString());
            invoices.add(tempInvoice);
        }
        return invoices;
    }

    // gör en invoiceRow med de items som är för alla, ska denna ha mer error
    // checking?
    private List<InvoiceRow> forAll(ArrayList<InvoiceItem> forAll) throws IllegalArgumentException {
        List<InvoiceRow> invoiceRows = new ArrayList<>();
        if (forAll == null) {
            throw new IllegalArgumentException("Det finns inga items för alla");
        }
        try {
            for (InvoiceItem item : forAll) {
                if (item.forAll) {
                    InvoiceRow row = new InvoiceRow();
                    row.setArticleName(item.key);
                    row.setArticleNumber(item.articleNbr);
                    row.setDeliveredQuantity(1); // ska denna alltid vara 1? varför inte hårdkoda isåfall?
                    row.setPrice(item.price);
                    invoiceRows.add(row);
                }
            }
        } catch (Exception e) {
            System.out.println("Något gick fel vid skapandet av rabatterna för alla");
            e.getMessage();
        }

        return invoiceRows;
    }

    private ArrayList<InvoiceRow> toRows(String[] items, ArrayList<InvoiceItem> itemFilter) {
        ArrayList<InvoiceRow> rows = new ArrayList<>();
        for (String item : items) {
            item = item.trim();
            for (InvoiceItem invoiceItems : itemFilter) {
                if (item.equals(invoiceItems.key)) {
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

    private InvoiceRow discount(String discount, ArrayList<InvoiceItem> discountList) {
        for (InvoiceItem invoiceItems : discountList) {
            if (discount.contains(invoiceItems.key)) {
                InvoiceRow tempInvoiceRow = new InvoiceRow();
                tempInvoiceRow.setArticleName(invoiceItems.key);
                tempInvoiceRow.setArticleNumber(invoiceItems.articleNbr);
                tempInvoiceRow.setDeliveredQuantity(1);
                tempInvoiceRow.setPrice(invoiceItems.price);
                return tempInvoiceRow;
            } else
                throw new IllegalArgumentException("Rabatten finns inte i listan");
        }
        return null;
    }

}
