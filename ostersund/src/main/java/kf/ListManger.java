package kf;

import java.util.ArrayList;

public class ListManger {
    private ArrayList<InvoiceItem> discounts;
    private ArrayList<InvoiceItem> invoiceItems;
    private ArrayList<InvoiceItem> forAll;
    private ArrayList<InvoiceItem> extraItems;

    public ListManger() {
        this.discounts = InvoiceItem.testDiscountList();
        this.invoiceItems = new ArrayList<InvoiceItem>();
        this.forAll = InvoiceItem.testInvoiceItems();
        this.extraItems = InvoiceItem.testInvoiceItems();
    }






















    public ArrayList<InvoiceItem> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(ArrayList<InvoiceItem> discounts) {
        this.discounts = discounts;
    }

    public ArrayList<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(ArrayList<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public ArrayList<InvoiceItem> getExtraItems() {
        return extraItems;
    }

    public void setExtraItems(ArrayList<InvoiceItem> extraItems) {
        this.extraItems = extraItems;
    }

    public ArrayList<InvoiceItem> getForAll() {
        return forAll;
    }

    public void setForAll(ArrayList<InvoiceItem> forAll) {
        this.forAll = forAll;
    }
    
}

