package kf.api;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

import kf.InvoiceItem;

public class Invoice {
    @SerializedName("CustomerNumber")
    private String customerNumber;

    @SerializedName("InvoiceDate")
    private String invoiceDate;

    @SerializedName("InvoiceRows")
    private List<InvoiceRow> invoiceRows;

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

    public void setInvoiceRows(List<InvoiceRow> invoiceRows) {
        this.invoiceRows = invoiceRows;
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

    public void setForAll(ArrayList<InvoiceItem> forAll) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setForAll'");
    }
}
