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

/**
 * A CSV file parser specifically designed for processing invoice-related data.
 * This class reads and validates CSV files containing customer and invoice information,
 * and converts the data into Invoice objects.
 * 
 * The CSV file must contain at least the following columns:
 * - "Rabatt" (Discount)
 * - "Grupp/Lag/Arbetsrum/Familj" (Group/Team/Workspace/Family)
 * - "Förnamn" (First name)
 * - "Efternamn" (Last name)
 * - "IdrottsID" (Sports ID)
 * 
 * The parser handles quoted CSV fields and validates the structure of the CSV file.
 */
public class Parser {
    private final ArrayList<String[]> data = new ArrayList<>();
    private final File file;
    private final HashMap<String, Integer> header = new HashMap<>();

    /**
     * Constructs a Parser object and initiates parsing of the specified file.
     * 
     * @param file The File object to be parsed
     * @throws IllegalArgumentException if the file is null or does not exist
     */
    public Parser(File file) {
        this.file = file;
        parse();
    }

    /**
     * Parses the CSV file and initializes the data structure.
     * Reads the file line by line, stores the data in a list,
     * creates a header mapping, and validates the CSV structure.
     * The first line is treated as the header row.
     *
     * @throws IllegalArgumentException if the file is not found,
     *         if the CSV file is empty, or if the CSV validation fails
     */
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

    /**
     * Validates the structure and content of the CSV data.
     * 
     * @throws IllegalArgumentException if required columns 'Rabatt' or 'Grupp/Lag/Arbetsrum/Familj' are missing
     * @throws IllegalArgumentException if any row has a different number of columns than the first row
     */
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

    /**
     * Parses a single line of CSV data and returns an array of fields.
     * Handles quoted fields and commas within quotes.
     * 
     * @param line The line of CSV data to be parsed
     * @return An array of fields extracted from the line
     */
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

    /**
     * Retrieves the value of a specific column in the CSV data.
     * 
     * @param index The index of the row in the CSV data
     * @param columnName The name of the column to retrieve
     * @return The value of the specified column in the specified row
     * @throws IllegalArgumentException if the column name is not found in the header
     *         or if the column value is missing in the CSV data
     */
    private String getColumnValue(int index, String columnName) {
        Integer colIndex = header.get(columnName);
        if (colIndex == null || index >= data.size() || colIndex >= data.get(index).length) {
            throw new IllegalArgumentException(
                    "Värde för kolumn '" + columnName + "' på rad " + (index + 1) + " saknas i CSV-filen.");
        }
        return data.get(index)[colIndex];
    }

    /**
     * Converts the parsed CSV data into a list of Invoice objects.
     * 
     * @param extraItems A list of additional invoice items to be added to the invoices
     * @param forAllList A list of invoice items that should be included in all invoices
     * @param discountList A list of invoice items that represent discounts
     * @return A list of Invoice objects containing the parsed data
     */
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

    /**
     * Creates a list of InvoiceRow objects for items that should be included in all invoices.
     * 
     * @param forAllList A list of invoice items that should be included in all invoices
     * @return A list of InvoiceRow objects containing the items that should be included in all invoices
     */
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

    /**
     * Converts an array of item keys into a list of InvoiceRow objects.
     * 
     * @param items An array of item keys to be converted
     * @param itemFilter A list of invoice items to filter the items against
     * @return A list of InvoiceRow objects containing the converted items
     */
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

    /**
     * Creates an InvoiceRow object for a discount item.
     * 
     * @param discount The discount item key
     * @param discountList A list of invoice items that represent discounts
     * @return An InvoiceRow object representing the discount item
     * @throws IllegalArgumentException if the discount item is not found in the discount list
     */
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