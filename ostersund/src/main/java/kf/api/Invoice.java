package kf.api;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;



/**
 * Represents an invoice in the system with customer details and invoice rows.
 * This class manages invoice-related data including customer information and line items.
 * 
 * The class uses the @SerializedName annotation to map JSON field names during serialization/deserialization.
 * 
 * @since 1.0
 */
public class Invoice {
    @SerializedName("CustomerNumber")
    private String customerNumber;

    @SerializedName("InvoiceDate")
    private String invoiceDate;

    @SerializedName("InvoiceRows")
    private List<InvoiceRow> invoiceRows = new ArrayList<InvoiceRow>();

    private transient String customerName; // Transient field for customer's name

    /*
     * Returns the customer number associated with the invoice.
     */
    public String getCustomerNumber() {
        return customerNumber;
    }

    /*
     * Sets the customer number associated with the invoice.
     * 
     * @param customerNumber The customer number to set
     */
    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    /*
     * Returns the invoice date.
     */
    public String getInvoiceDate() {
        return invoiceDate;
    }

    /*
     * Sets the invoice date.
     * 
     * @param invoiceDate The invoice date to set
     */
    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /*
     * Returns the list of invoice rows associated with the invoice.
     */
    public List<InvoiceRow> getInvoiceRows() {
        return invoiceRows;
    }

    /*
     * Sets the list of invoice rows associated with the invoice.
     * 
     * @param invoiceRows The list of invoice rows to set
     */
    public void addInvoiceRows(List<InvoiceRow> invoiceRows) {
        for (InvoiceRow row : invoiceRows) {
            this.invoiceRows.add(row);
        }
    }

    /*
     * Adds an invoice row to the list of invoice rows associated with the invoice.
     * 
     * @param row The invoice row to add
     */
    public void addInvoiceRow (InvoiceRow row) {
        invoiceRows.add(row);
    }

    /*
     * Returns the customer name associated with the invoice.
     */
    public String getCustomerName() {
        return customerName;
    }

    /*
     * Sets the customer name associated with the invoice.
     * 
     * @param customerName The customer name to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}

