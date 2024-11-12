package kf.api;

import com.google.gson.annotations.SerializedName;

public class InvoiceRow {
    @SerializedName("ArticleNumber")
    private String articleNumber;

    @SerializedName("DeliveredQuantity")
    private int deliveredQuantity;

    @SerializedName("Price")
    private double price;

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
}

