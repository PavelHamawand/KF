package kf;

import java.io.Serializable;

/**
 * Represents an invoice item with key, article number, price, and availability status.
 * This class implements Serializable for object persistence.
 * 
 * @author KForesund
 * @version 1.0
 */
public class InvoiceItem implements Serializable { 
    private static final long serialVersionUID = 1L;

    public String key;
    public String articleNbr;
    public double price;
    public boolean forAll;

    public InvoiceItem(String key, String articleNbr, double price) {
        this.key = key;
        this.articleNbr = articleNbr;
        this.price = price;
        this.forAll = false;

    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }

    // Try not to use this outside of ListManger
    public void toggleForAll() {
        this.forAll = !this.forAll;
    }
}

