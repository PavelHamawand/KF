package kf;

import java.util.ArrayList;

public class ListManger {
    private ArrayList<InvoiceItem> discounts;
    private ArrayList<InvoiceItem> invoiceItems;
    private ArrayList<InvoiceItem> forAll;
    private ArrayList<InvoiceItem> extraItems;

    public ListManger() {
        this.discounts = initDiscounts();
        this.invoiceItems = initInvoiceItems();
        //skapar forAll listan
        this.forAll = new ArrayList<>();
        
        //skapar extraItems listan och fyller den med alla items som inte är för alla vet ej om detta är rätt men tolkar det så av den bilden
        this.extraItems = new ArrayList<>();
        for (InvoiceItem item : invoiceItems) {
            if (item.forAll) {
                this.forAll.add(item);
            } else {
                this.extraItems.add(item);
            }
        }
    }

    private ArrayList<InvoiceItem> initForAll() {
        ArrayList<InvoiceItem> forAll = new ArrayList<InvoiceItem>();
        forAll.add(new InvoiceItem("Medlemsavgift", "IO-AVG-2743", 105.0));
        forAll.add(new InvoiceItem("Träningskort", "IO-AVG-43392", 880.0));
        return forAll;
    }

    private ArrayList<InvoiceItem> initInvoiceItems() {
        ArrayList<InvoiceItem>  invoiceItems = new ArrayList<InvoiceItem>();
        invoiceItems.add(new InvoiceItem("Medlemsavgift", "IO-AVG-2743", 105.0));
        invoiceItems.add(new InvoiceItem("Träningskort", "IO-AVG-43392", 880.0));
        invoiceItems.add(new InvoiceItem("Kajakplats", "IO-AVG-1478", 1450.0));
        invoiceItems.add(new InvoiceItem("Utökat träningskort", "IO-AVG-1479", 690.0));
        return invoiceItems;
    }

    private ArrayList<InvoiceItem> initDiscounts(){
        ArrayList<InvoiceItem> discountList = new ArrayList<InvoiceItem>();
        discountList.add(new InvoiceItem("1", "R1", -200));
        discountList.add(new InvoiceItem("2", "R2", -400));
        discountList.add(new InvoiceItem("3", "R3", -600));
        return discountList;
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

