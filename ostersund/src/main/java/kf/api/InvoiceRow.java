package kf.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a row in an invoice, containing article details and quantity information.
 * This class is used for serialization/deserialization of invoice data.
 * 
 * The class uses SerializedName annotations to map JSON fields to Java properties,
 * and includes transient fields for runtime data that should not be serialized.
 * 
* @since 1.0
* @author Berni
 */
public class InvoiceRow {
    /**
     * The article number associated with the invoice row.
     */
    @SerializedName("ArticleNumber")
    private String articleNumber;

    /**
     * The delivered quantity of the article.
     */
    @SerializedName("DeliveredQuantity")
    private int deliveredQuantity;

    /**
     * The price of the article.
     */
    private transient double price;

    /**
     * The name/description of the article.
     */
    private transient String articleName;

    /*
     * Returns the article number associated with the invoice row.
     */
    public String getArticleNumber() {
        return articleNumber;
    }

    /*
     * Sets the article number associated with the invoice row.
     * 
     * @param articleNumber The article number to set
     */
    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    /*
     * Returns the delivered quantity of the article.
     */
    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    /*
     * Sets the delivered quantity of the article.
     * 
     * @param deliveredQuantity The delivered quantity to set
     */
    public void setDeliveredQuantity(int deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    /*
     * Returns the price of the article.
     */
    public double getPrice() {
        return price;
    }

    /*
     * Sets the price of the article.
     * 
     * @param price The price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /*
     * Returns the name/description of the article.
     */
    public String getArticleName() {
        return articleName;
    }

    /*
     * Sets the name/description of the article.
     * 
     * @param articleName The article name to set
     */
    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }
}
