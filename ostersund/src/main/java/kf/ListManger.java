package kf;

import java.io.Serializable;
import java.util.ArrayList;

public class ListManger implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<InvoiceItem> discounts;
    private ArrayList<InvoiceItem> invoiceItems;
    private ArrayList<InvoiceItem> forAll;
    private ArrayList<InvoiceItem> extraItems;

    public ListManger() {
        this.discounts = initDiscounts();
        this.invoiceItems = initInvoiceItems();
        this.forAll = new ArrayList<>();
        this.extraItems = new ArrayList<>();
        sortList();
        
    }
    
    private void sortList() {
        for (InvoiceItem item : invoiceItems) {
            
            if (item.forAll) {
                
                if(forAll.contains(item)) {
                    continue;
                }
                else this.forAll.add(item);
            } 

            else if (extraItems.contains(item)) {
                continue;
            } 
            else {
                    extraItems.add(item);
            }
        }
    }
    private ArrayList<InvoiceItem> initInvoiceItems() {
        ArrayList<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();
        invoiceItems.add(new InvoiceItem("Medlemsavgift", "IO-AVG-2743", 105.0));
        invoiceItems.add(new InvoiceItem("Träningskort", "IO-AVG-43392", 880.0));
        invoiceItems.add(new InvoiceItem("Kajakplats", "IO-AVG-1478", 1450.0));
        invoiceItems.add(new InvoiceItem("Kajakplats x2", "IO-AVG-1826", 1450.0));
        invoiceItems.add(new InvoiceItem("Utökat träningskort", "IO-AVG-1479", 690.0));

        for (int i = 0; i < invoiceItems.size() / 2; i++) {
            invoiceItems.get(i).toggleForAll();
        }

        return invoiceItems;
    }

    private ArrayList<InvoiceItem> initDiscounts() {
        ArrayList<InvoiceItem> discountList = new ArrayList<InvoiceItem>();
        discountList.add(new InvoiceItem("Rabatt x1", "IO-AVG-1480", -200));
        discountList.add(new InvoiceItem("Rabatt x2", "IO-AVG-1481", -400));
        discountList.add(new InvoiceItem("Rabatt x3", "IO-AVG-1482", -600));
        return discountList;
    }

    public void editItem(InvoiceItem item, String name, String articleNbr, double price) {
        invoiceItems.remove(item);
        extraItems.remove(item);
        forAll.remove(item);
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        invoiceItems.add(newItem);
        sortList();
        System.out.println("Item edited" + newItem.price);
    }

    public void editDiscount(InvoiceItem item, String name, String articleNbr, double price) {
        discounts.remove(item);
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        discounts.add(newItem);
        System.out.println("Item edited" + newItem.price);
    }

    public void addInvoiceItem(String name, String articleNbr, double price) throws IllegalArgumentException {
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        if (invoiceItems.contains(newItem)) {
            throw new IllegalArgumentException("Article number already exists");
        } else
            invoiceItems.add(newItem);
    }

    public void addDiscount(String name, String articleNbr, double price) throws IllegalArgumentException {
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        if (discounts.contains(newItem)) {
            throw new IllegalArgumentException("Article number already exists");
        } else
            discounts.add(newItem);
    }

    public void toggleForAll(InvoiceItem item) {
        item.toggleForAll();
        if(!item.forAll) {
            forAll.remove(item);
            extraItems.add(item);
        } else {
            extraItems.remove(item);
            forAll.add(item);
        }
    }

    public void remove(InvoiceItem item) {
        // Ta bort item från alla listor, snabbare och säkrare än att söka efter referensen i alla listor
        invoiceItems.remove(item);
        extraItems.remove(item);
        forAll.remove(item);
    }

    public void removeDiscount(InvoiceItem item) {
        discounts.remove(item);
    }

    public ArrayList<InvoiceItem> getDiscounts() {
        return discounts;
    }

    public ArrayList<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public ArrayList<InvoiceItem> getExtraItems() {
        return extraItems;
    }

    public ArrayList<InvoiceItem> getForAll() {
        return forAll;
    }

}
