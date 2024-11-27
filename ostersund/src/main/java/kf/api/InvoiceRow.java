package kf.api;

import com.google.gson.annotations.SerializedName;

public class InvoiceRow {
    @SerializedName("ArticleNumber")
    private String articleNumber;

    @SerializedName("DeliveredQuantity")
    private int deliveredQuantity;


    private transient double price;

    private transient String articleName; // Transient field for article's name

    // Getters and Setters
    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(int deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }
}

