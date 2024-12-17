package kf;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The ListManger class is responsible for managing lists of InvoiceItem objects.
 * It implements the Serializable interface to allow its instances to be serialized.
 * The class maintains four lists: discounts, invoiceItems, forAll, and extraItems.
 * It provides methods to initialize, sort, edit, add, toggle, and remove items from these lists.
 * 
 * <p>Methods:</p>
 * <ul>
 *   <li>{@link #ListManager()}: Constructor that initializes the lists and sorts the invoice items.</li>
 *   <li>{@link #sortList()}: Sorts the invoice items into forAll and extraItems lists based on their forAll property.</li>
 *   <li>{@link #initInvoiceItems()}: Initializes the invoiceItems list with predefined items and toggles the forAll property for half of them.</li>
 *   <li>{@link #initDiscounts()}: Initializes the discounts list with predefined discount items.</li>
 *   <li>{@link #editItem(InvoiceItem, String, String, double)}: Edits an existing invoice item and updates the lists accordingly.</li>
 *   <li>{@link #editDiscount(InvoiceItem, String, String, double)}: Edits an existing discount item and updates the discounts list.</li>
 *   <li>{@link #addInvoiceItem(String, String, double)}: Adds a new invoice item to the invoiceItems list and sorts the list.</li>
 *   <li>{@link #addDiscount(String, String, double)}: Adds a new discount item to the discounts list.</li>
 *   <li>{@link #toggleForAll(InvoiceItem)}: Toggles the forAll property of an invoice item and updates the forAll and extraItems lists.</li>
 *   <li>{@link #remove(InvoiceItem)}: Removes an invoice item from all lists.</li>
 *   <li>{@link #removeDiscount(InvoiceItem)}: Removes a discount item from the discounts list.</li>
 *   <li>{@link #getDiscounts()}: Returns the discounts list.</li>
 *   <li>{@link #getInvoiceItems()}: Returns the invoiceItems list.</li>
 *   <li>{@link #getExtraItems()}: Returns the extraItems list.</li>
 *   <li>{@link #getForAll()}: Returns the forAll list.</li>
 * </ul>
 */
public class ListManager implements Serializable {

    /**
     * The serial version UID is a universal version identifier for a Serializable class.
     * Deserialization uses this number to ensure that a loaded class corresponds exactly to a serialized object.
     * If no match is found, then an InvalidClassException is thrown.
     */
    private static final long serialVersionUID = 1L;

    /*
     * The lists of Discounts
     */
    private ArrayList<InvoiceItem> discounts;

    /*
     * The lists of InvoiceItems
     */
    private ArrayList<InvoiceItem> invoiceItems;

    /*
     * The lists of InvoiceItems that are marked as forAll
     */
    private ArrayList<InvoiceItem> forAll;

    /*
     * The lists of InvoiceItems that are not marked as forAll, 
     * these are the ones displayed in the invoice generator
     */
    private ArrayList<InvoiceItem> extraItems;

    /**
     * Constructor for the ListManger class.
     * Initializes the discounts, invoiceItems, forAll, and extraItems lists.
     * Also sorts the list after initialization.
     */
    public ListManager() {
        this.discounts = initDiscounts();
        this.invoiceItems = initInvoiceItems();
        this.forAll = new ArrayList<>();
        this.extraItems = new ArrayList<>();
        sortList();

    }

    /**
     * Sorts the list of invoice items into two separate lists: forAll and extraItems.
     * 
     * The method iterates through each item in the invoiceItems list and performs the following actions:
     * - If the item is marked as forAll and is not already in the forAll list, it adds the item to the forAll list.
     * - If the item is not marked as forAll and is not already in the extraItems list, it adds the item to the extraItems list.
     * 
     * The method ensures that each item is added to the appropriate list only once.
     */
    private void sortList() {
        for (InvoiceItem item : invoiceItems) {

            if (item.forAll) {

                if (forAll.contains(item)) {
                    continue;
                } else
                    this.forAll.add(item);
            }

            else if (extraItems.contains(item)) {
                continue;
            } else {
                extraItems.add(item);
            }
        }
    }

    /**
     * Initializes a list of InvoiceItem objects with predefined data.
     * 
     * @return An ArrayList of InvoiceItem objects with sample data.
     */
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

    /**
     * Initializes a list of InvoiceItem objects with predefined discount data.
     * 
     * @return An ArrayList of InvoiceItem objects with sample discount data.
     */
    private ArrayList<InvoiceItem> initDiscounts() {
        ArrayList<InvoiceItem> discountList = new ArrayList<InvoiceItem>();
        discountList.add(new InvoiceItem("Rabatt x1", "IO-AVG-1480", -200));
        discountList.add(new InvoiceItem("Rabatt x2", "IO-AVG-1481", -400));
        discountList.add(new InvoiceItem("Rabatt x3", "IO-AVG-1482", -600));
        return discountList;
    }

    /**
     * Edits an existing invoice item with new data and updates the lists accordingly.
     * 
     * The method removes the existing item from the lists, creates a new item with the updated data,
     * and adds the new item to the invoiceItems list. It then sorts the list to update the forAll and extraItems lists.
     * 
     * @param item The existing InvoiceItem object to be edited.
     * @param name The new name for the item.
     * @param articleNbr The new article number for the item.
     * @param price The new price for the item.
     */
    public void editItem(InvoiceItem item, String name, String articleNbr, double price) {
        invoiceItems.remove(item);
        extraItems.remove(item);
        forAll.remove(item);
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        invoiceItems.add(newItem);
        sortList();
        System.out.println("Item edited" + newItem.price);
    }

    /**
     * Edits an existing discount item with new data and updates the discounts list.
     * 
     * The method removes the existing discount item from the discounts list and creates a new discount item
     * with the updated data. It then adds the new discount item to the discounts list.
     * 
     * @param item The existing InvoiceItem object to be edited.
     * @param name The new name for the discount item.
     * @param articleNbr The new article number for the discount item.
     * @param price The new price for the discount item.
     */
    public void editDiscount(InvoiceItem item, String name, String articleNbr, double price) {
        if (price > 0) {
            throw new IllegalArgumentException("Price must be negative");
        }
        discounts.remove(item);
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        discounts.add(newItem);
        System.out.println("Item edited" + newItem.price);
    }

    /**
     * Adds a new invoice item to the invoiceItems list and sorts the list.
     * 
     * The method creates a new InvoiceItem object with the provided data and adds it to the invoiceItems list.
     * It then sorts the list to update the forAll and extraItems lists.
     * 
     * @param name The name of the new item.
     * @param articleNbr The article number of the new item.
     * @param price The price of the new item.
     * @throws IllegalArgumentException if the article number already exists in the list.
     */
    public void addInvoiceItem(String name, String articleNbr, double price) throws IllegalArgumentException {
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        if (invoiceItems.contains(newItem)) {
            throw new IllegalArgumentException("Article number already exists");
        } else
            invoiceItems.add(newItem);
            sortList();
    }

    /**
     * Adds a new discount item to the discounts list.
     * 
     * The method creates a new InvoiceItem object with the provided data and adds it to the discounts list.
     * 
     * @param name The name of the new discount item.
     * @param articleNbr The article number of the new discount item.
     * @param price The price of the new discount item (must be negative).
     * @throws IllegalArgumentException if the price is not negative or if the article number already exists in the list.
     */
    public void addDiscount(String name, String articleNbr, double price) throws IllegalArgumentException {
        if (price > 0) {
            throw new IllegalArgumentException("Price must be negative");
        }
        InvoiceItem newItem = new InvoiceItem(name, articleNbr, price);
        if (discounts.contains(newItem)) {
            throw new IllegalArgumentException("Article number already exists");
        } else
            discounts.add(newItem);
    }

    /**
     * Toggles the forAll property of an invoice item and updates the forAll and extraItems lists.
     * 
     * The method toggles the forAll property of the provided item and updates the forAll and extraItems lists accordingly.
     * If the item is marked as forAll, it is removed from the extraItems list and added to the forAll list.
     * If the item is not marked as forAll, it is removed from the forAll list and added to the extraItems list.
     * 
     * @param item The InvoiceItem object for which to toggle the forAll property.
     */
    public void toggleForAll(InvoiceItem item) {
        item.toggleForAll();
        if (!item.forAll) {
            forAll.remove(item);
            extraItems.add(item);
        } else {
            extraItems.remove(item);
            forAll.add(item);
        }
    }

    /**
     * Removes an invoice item from all lists.
     * 
     * The method removes the provided item from the invoiceItems, extraItems, and forAll lists.
     * 
     * @param item The InvoiceItem object to be removed.
     */
    public void remove(InvoiceItem item) {
        invoiceItems.remove(item);
        extraItems.remove(item);
        forAll.remove(item);
    }

    /**
     * Removes a discount item from the discounts list.
     * 
     * The method removes the provided discount item from the discounts list.
     * 
     * @param item The InvoiceItem object to be removed.
     */
    public void removeDiscount(InvoiceItem item) {
        discounts.remove(item);
    }

    /**
     * Returns the discounts list.
     * 
     * @return An ArrayList of InvoiceItem objects representing the discounts list.
     */
    public ArrayList<InvoiceItem> getDiscounts() {
        return discounts;
    }

    /**
     * Returns the invoiceItems list.
     * 
     * @return An ArrayList of InvoiceItem objects representing the invoiceItems list.
     */
    public ArrayList<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    /**
     * Returns the extraItems list.
     * 
     * @return An ArrayList of InvoiceItem objects representing the extraItems list.
     */
    public ArrayList<InvoiceItem> getExtraItems() {
        return extraItems;
    }

    /**
     * Returns the forAll list.
     * 
     * @return An ArrayList of InvoiceItem objects representing the forAll list.
     */
    public ArrayList<InvoiceItem> getForAll() {
        return forAll;
    }

}
