package kf;

public class InvoiceItem { // dåligt namn icke plural
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

    public void toggleForAll() {
        this.forAll = !this.forAll;
    }
}
