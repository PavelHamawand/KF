package kf.api;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Invoice {
    @SerializedName("CustomerNumber")
    private String customerNumber;

    @SerializedName("InvoiceDate")
    private String invoiceDate;

    @SerializedName("InvoiceRows")
    private List<InvoiceRow> invoiceRows = new ArrayList<InvoiceRow>();

    private transient String customerName; // Transient field for customer's name

    // Getters and Setters
    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public List<InvoiceRow> getInvoiceRows() {
        return invoiceRows;
    }

    public void addInvoiceRows(List<InvoiceRow> invoiceRows) {
        for (InvoiceRow row : invoiceRows) {
            this.invoiceRows.add(row);
        }
    }

    public void addInvoiceRow (InvoiceRow row) {
        invoiceRows.add(row);
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}

