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
    private final ArrayList<String[]> data = new ArrayList<>();
    private final File file;
    private final HashMap<String, Integer> header = new HashMap<>();

    public Parser(File file) {
        this.file = file;
        parse();
    }

    private void parse() throws IllegalArgumentException {
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                data.add(parseLine(line));
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Fil hittades inte: " + file.getAbsolutePath(), e);
        }

        if (data.isEmpty()) {
            throw new IllegalArgumentException("CSV-filen är tom.");
        }

        for (int i = 0; i < data.get(0).length; i++) {
            header.put(data.get(0)[i], i);
        }

        validateCSV();
    }

    private void validateCSV() {
        if (!header.containsKey("Rabatt")) {
            throw new IllegalArgumentException("Kolumnen 'Rabatt' saknas i CSV-filen.");
        }
        if (!header.containsKey("Grupp/Lag/Arbetsrum/Familj")) {
            throw new IllegalArgumentException("Kolumnen 'Grupp/Lag/Arbetsrum/Familj' saknas i CSV-filen.");
        }

        int expectedLength = data.get(0).length;
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).length != expectedLength) {
                throw new IllegalArgumentException("Fel antal kolumner på rad " + (i + 1));
            }
        }
    }

    private String[] parseLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }

    private String getColumnValue(int index, String columnName) {
        Integer colIndex = header.get(columnName);
        if (colIndex == null || index >= data.size() || colIndex >= data.get(index).length) {
            throw new IllegalArgumentException(
                    "Värde för kolumn '" + columnName + "' på rad " + (index + 1) + " saknas i CSV-filen.");
        }
        return data.get(index)[colIndex];
    }

    public ArrayList<Invoice> toInvoices(ArrayList<InvoiceItem> extraItems,
                                         ArrayList<InvoiceItem> forAllList,
                                         ArrayList<InvoiceItem> discountList) {
        ArrayList<Invoice> invoices = new ArrayList<>();
        List<InvoiceRow> forAllRows = createRowsForAll(forAllList);

        for (int x = 1; x < data.size(); x++) {
            Invoice invoice = new Invoice();
            invoice.setCustomerName(getColumnValue(x, "Förnamn") + " " + getColumnValue(x, "Efternamn"));
            invoice.setCustomerNumber(getColumnValue(x, "IdrottsID"));
            invoice.addInvoiceRows(forAllRows);

            try {
                String groupValue = getColumnValue(x, "Grupp/Lag/Arbetsrum/Familj");
                if (!groupValue.isBlank()) {
                    invoice.addInvoiceRows(toRows(groupValue.split(","), extraItems));
                }
            } catch (Exception e) {
                System.err.println("Fel vid skapandet av extra tjänster för rad " + (x + 1));
                e.printStackTrace();
            }

            try {
                String discountValue = getColumnValue(x, "Rabatt");
                if (!discountValue.isBlank()) {
                    invoice.addInvoiceRow(createDiscountRow(discountValue, discountList));
                }
            } catch (Exception e) {
                System.err.println("Fel vid skapandet av rabatten för rad " + (x + 1));
                e.printStackTrace();
            }

            invoice.setInvoiceDate(LocalDate.now().toString());
            invoices.add(invoice);
        }
        return invoices;
    }

    private List<InvoiceRow> createRowsForAll(ArrayList<InvoiceItem> forAllList) {
        List<InvoiceRow> rows = new ArrayList<>();
        if (forAllList == null) {
            throw new IllegalArgumentException("Listan 'forAllList' är null.");
        }
        for (InvoiceItem item : forAllList) {
            if (item.forAll) {
                InvoiceRow row = new InvoiceRow();
                row.setArticleName(item.key);
                row.setArticleNumber(item.articleNbr);
                row.setDeliveredQuantity(1);
                row.setPrice(item.price);
                rows.add(row);
            }
        }
        return rows;
    }

    private ArrayList<InvoiceRow> toRows(String[] items, ArrayList<InvoiceItem> itemFilter) {
        ArrayList<InvoiceRow> rows = new ArrayList<>();
        for (String item : items) {
            for (InvoiceItem invoiceItem : itemFilter) {
                if (item.trim().equals(invoiceItem.key)) {
                    InvoiceRow row = new InvoiceRow();
                    row.setArticleName(invoiceItem.key);
                    row.setArticleNumber(invoiceItem.articleNbr);
                    row.setDeliveredQuantity(1);
                    row.setPrice(invoiceItem.price);
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private InvoiceRow createDiscountRow(String discount, ArrayList<InvoiceItem> discountList) {
        for (InvoiceItem item : discountList) {
            if (discount.contains(item.key)) {
                InvoiceRow row = new InvoiceRow();
                row.setArticleName(item.key);
                row.setArticleNumber(item.articleNbr);
                row.setDeliveredQuantity(1);
                row.setPrice(item.price);
                return row;
            }
        }
        throw new IllegalArgumentException("Rabatten '" + discount + "' finns inte i listan.");
    }
}