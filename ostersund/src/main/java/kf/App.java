package kf;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import kf.api.Api;
import kf.api.Invoice;
import kf.api.InvoiceRow;

import java.io.File;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main application class for KF Öresund invoice management system.
 * This class provides a JavaFX GUI application for managing invoices, invoice
 * items, and discounts.
 * It allows users to:
 * - Select and process CSV files containing invoice data
 * - Manage invoice items and discounts
 * - Generate and send invoices to Fortnox
 * - Save and load application state through serialization
 *
 * The application consists of multiple scenes:
 * - File selection scene
 * - Main menu scene
 * - Invoice generation scene
 * - Invoice items management scene
 * - Discounts management scene
 * - Add/Edit items scenes
 *
 * Extends Application JavaFX Application class
 */
public class App extends Application {
    private File selectedFile;
    private ListManager lm = null;
    private final int width = 800;
    private final int heigth = 600;

    /**
     * Main method to launch the JavaFX application.
     */
    @Override
    public void start(Stage s) {
        s.setTitle("KF Öresund");
        s.setScene(getSelectFileScene(s));
        s.show();
        setListManager(this.lm);
    }

    /**
     * Sets the ListManger instance by attempting to deserialize from a file.
     * If the file 'listManger.ser' exists, it deserializes the ListManger object
     * from it.
     * If the file doesn't exist, it creates a new ListManger instance.
     *
     * @param lm The ListManger parameter (unused in current implementation)
     * @throws ClassNotFoundException if the ListManger class cannot be found during
     *                                deserialization
     */
    private void setListManager(ListManager lm) {
        try (FileInputStream fileIn = new FileInputStream("listManger.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn)) {
            this.lm = (ListManager) in.readObject();
            System.out.println("Deserialized ListManger...");
        } catch (IOException i) {
            this.lm = new ListManager();
            System.out.println("Cant find listManger.ser, creating new ListManger...");
        } catch (ClassNotFoundException c) {
            System.out.println("ListManger class not found");
        }
    }

    /**
     * Saves the ListManger object to a file through serialization.
     * The serialized object is stored in "listManger.ser" file.
     * 
     * @param lm The ListManger object to be serialized and saved
     */
    private void saveListManager(ListManager lm) {
        try {
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream("listManger.ser");
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fileOut);
            out.writeObject(lm);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in listManger.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Creates and returns a Scene for file selection.
     * The scene contains a title, instruction text, and a browse button that opens
     * a file chooser dialog restricted to CSV files.
     * When a file is selected, it transitions to the main layout scene.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for file selection
     */
    private Scene getSelectFileScene(Stage stage) {
        Label titleLabel = createLabel("Select your csv file", "label");
        Label instructionLabel = createLabel("Please provide a correctly formatted csv", "instructions-text");

        Button selectFileButton = createButton("Browse files...", "menu-button", event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                stage.setScene(getMainLayout(stage));
            }
        });

        VBox layout = createVBox(Pos.CENTER, 10, titleLabel, instructionLabel, selectFileButton);
        layout.getStyleClass().add("vbox-container");

        Scene scene = new Scene(new StackPane(layout), width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates and returns the main layout scene for the application.
     * The scene contains a menu with buttons for generating invoices, managing
     * invoice items, and discounts.
     * It also includes instructions on how to use the application.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for the main layout
     */
    private Scene getMainLayout(Stage stage) {
        VBox buttonMenu = createVBox(Pos.CENTER_LEFT, 20,
                createLabel("Menu:", "label"),
                createMenuButton("Generate Invoice", stage, this::getInvoicesScene),
                createMenuButton("Invoice Items", stage, this::getInvoiceItemsScene),
                createMenuButton("Discounts", stage, this::getDiscountScene),
                createButton("Exit", "menu-button", e -> stage.close()));
        buttonMenu.setPadding(new Insets(30));

        VBox instructions = createVBox(Pos.CENTER_LEFT, 10,
                createLabel("How to use:", "label"),
                createText(
                        "Generate Invoices:\nGenerates invoices based on your provided CSV file\n" +
                                "with the use of your active invoice items.\n\n" +
                                "Invoice Items:\nAdd the items you wish to have active\n" +
                                "for your invoice generation.\n\n" +
                                "Discounts: \nThe discounts are articles set in fortnox "),
                createButton("Change csv file", "menu-button", e -> stage.setScene(getSelectFileScene(stage))));
        instructions.setPadding(new Insets(30));

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setLeft(buttonMenu);
        mainLayout.setRight(instructions);

        Scene scene = new Scene(mainLayout, width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates and returns the invoice generation scene for the application.
     * The scene contains a title, a table for displaying generated invoices, and
     * buttons for sending invoices to Fortnox.
     * It also includes a back button to return to the main layout.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for invoice generation
     */
    private Scene getInvoicesScene(Stage stage) {
        // Titel
        Label titleLabel = createLabel("Generate Invoices", "label");

        // Tabell för fakturor
        TableView<List<String>> invoiceTable = createInvoiceTable();

        // Populera tabellen med data från vald fil
        ArrayList<Invoice> invoices = populateTable(invoiceTable, selectedFile);

        // Skapa layout för knappar
        Button backButton = createButton("Back", "menu-button", e -> stage.setScene(getMainLayout(stage)));
        Button sendButton = createButton("Send to Fortnox", "menu-button", e -> {
            if (invoices.isEmpty()) {
                showAlert("No Data", "No invoices to send. Please check your file.");
            } else {
                sendToFortnox(invoices);
            }
        });

        HBox buttonLayout = createHBox(Pos.CENTER, 20, backButton, sendButton);

        // Skapa huvudlayout
        VBox layout = createVBox(Pos.TOP_CENTER, 10,
                titleLabel,
                invoiceTable,
                buttonLayout);
        layout.setPadding(new Insets(30));

        // Returnera scenen med styling
        Scene scene = new Scene(layout, width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates and returns the invoice items management scene for the application.
     * The scene contains a list of invoice items with checkboxes to include items
     * in all invoices.
     * It also includes buttons for adding, editing, and removing invoice items.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for invoice items management
     */
    private Scene getInvoiceItemsScene(Stage stage) {
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));
        itemList.setAlignment(Pos.BASELINE_LEFT);

        HBox headerRow = new HBox(10);
        headerRow.getChildren().addAll(
                createLabel("For All", "instructions-text"),
                createLabel("Item", "instructions-text"));
        // Add spacing to align with checkboxes
        headerRow.setMargin(headerRow.getChildren().get(1), new Insets(0, 0, 0, 20));

        itemList.getChildren().add(headerRow);

        for (InvoiceItem item : lm.getInvoiceItems()) {
            HBox itemRow = createHBox(Pos.CENTER_LEFT, 15,
                    createCheckBox(item),
                    createLabel(item.key, "instructions-text"),
                    createButton("Edit", "menu-button", e -> stage.setScene(getEditItemScene(stage, item))),
                    createButton("Remove", "remove-button", e -> {
                        lm.remove(item);
                        stage.setScene(getInvoiceItemsScene(stage));
                    })

            );
            if (item.forAll) {
                ((CheckBox) itemRow.getChildren().get(0)).setSelected(true);
            }
            itemList.getChildren().add(itemRow);
        }

        VBox tooltipBox = createTooltipBox("Tool tip",
                "Remember to use the\nsame name for the item\nhere as it is declared in\nthe provided CSV file." +
                        "\n\nCheck the box to include\nthe item in all invoices.");

        HBox buttonLayout = createHBox(Pos.CENTER, 20,
                createButton("Back", "menu-button", e -> {
                    saveListManager(lm);
                    stage.setScene(getMainLayout(stage));
                }),
                createButton("Add new Item", "menu-button", e -> stage.setScene(getAddItemScene(stage))));

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30));
        mainLayout.setLeft(new VBox(10, createLabel("Invoice Items", "instructions-text"), itemList));
        mainLayout.setRight(tooltipBox);
        mainLayout.setBottom(buttonLayout);

        Scene scene = new Scene(mainLayout, width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates and returns the add item scene for the application.
     * The scene contains input fields for adding a new invoice item with a name,
     * article number, and price.
     * It also includes buttons for saving the new item and returning to the invoice
     * items management scene.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for adding a new invoice item
     */
    private Scene getAddItemScene(Stage stage) {
        return createEditOrAddScene(stage, "Add New Invoice Item", null, false,
                newItem -> lm.addInvoiceItem(newItem.key, newItem.articleNbr, newItem.price),
                () -> stage.setScene(getInvoiceItemsScene(stage)));
    }

    /**
     * Creates and returns the edit item scene for the application.
     * The scene contains input fields for editing an existing invoice item with a
     * name, article number, and price.
     * It also includes buttons for saving the changes, removing the item, and
     * returning to the invoice items management scene.
     *
     * @param stage The primary stage of the application
     * @param item  The InvoiceItem object to be edited
     * @return A Scene object configured for editing an existing invoice item
     */
    private Scene getEditItemScene(Stage stage, InvoiceItem item) {
        return createEditOrAddScene(stage, "Edit Item", item, true,
                updatedItem -> lm.editItem(item, updatedItem.key, updatedItem.articleNbr, updatedItem.price),
                () -> stage.setScene(getInvoiceItemsScene(stage)));
    }

    /**
     * Creates and returns the discounts management scene for the application.
     * The scene contains a list of discounts with buttons for editing and removing
     * discounts.
     * It also includes a button for adding new discounts and a back button to
     * return to the main layout.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for discounts management
     */
    private Scene getDiscountScene(Stage stage) {
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));
        itemList.setAlignment(Pos.CENTER_LEFT);

        for (InvoiceItem discount : lm.getDiscounts()) {
            HBox discountRow = createHBox(Pos.TOP_LEFT, 10,
                    createLabel(discount.key, "instructions-text"),
                    createButton("Edit", "menu-button", e -> stage.setScene(getEditDiscountScene(stage, discount))),
                    createButton("Remove", "remove-button", e -> {
                        lm.removeDiscount(discount);
                        stage.setScene(getDiscountScene(stage));
                    }));
            itemList.getChildren().add(discountRow);
        }

        VBox tooltipBox = createTooltipBox("Tool tip",
                "Manage discounts here.\nUse names consistent\nwith your CSV file.");
        tooltipBox.setAlignment(Pos.TOP_LEFT);
        ;

        HBox buttonLayout = createHBox(Pos.CENTER, 20,
                createButton("Back", "menu-button", e -> {
                    saveListManager(lm);
                    stage.setScene(getMainLayout(stage));
                }),
                createButton("Add new Discount", "menu-button", e -> stage.setScene(getAddDiscountScene(stage))));

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(30));
        layout.setLeft(new VBox(10, createLabel("Discounts", "instructions-text"), itemList));
        layout.setRight(tooltipBox);
        layout.setBottom(buttonLayout);

        Scene scene = new Scene(layout, width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates and returns the edit discount scene for the application.
     * The scene contains input fields for editing an existing discount with a name,
     * article number, and price.
     * It also includes buttons for saving the changes, removing the discount, and
     * returning to the discounts management scene.
     *
     * @param stage    The primary stage of the application
     * @param discount The InvoiceItem object to be edited
     * @return A Scene object configured for editing an existing discount
     */
    private Scene getEditDiscountScene(Stage stage, InvoiceItem discount) {
        return createEditOrAddScene(stage, "Edit Discount", discount, true,
                updatedDiscount -> lm.editDiscount(discount, updatedDiscount.key, updatedDiscount.articleNbr,
                        updatedDiscount.price),
                () -> stage.setScene(getDiscountScene(stage)));
    }

    /**
     * Creates and returns the add discount scene for the application.
     * The scene contains input fields for adding a new discount with a name,
     * article number, and price.
     * It also includes buttons for saving the new discount and returning to the
     * discounts management scene.
     *
     * @param stage The primary stage of the application
     * @return A Scene object configured for adding a new discount
     */
    private Scene getAddDiscountScene(Stage stage) {
        return createEditOrAddScene(stage, "Add New Discount", null, false,
                newDiscount -> lm.addDiscount(newDiscount.key, newDiscount.articleNbr, newDiscount.price),
                () -> stage.setScene(getDiscountScene(stage)));
    }

    /**
     * Populates the invoice table with data from the selected CSV file.
     * The method reads the CSV file and generates invoices based on the data.
     * It then populates the table with customer names, items, and amounts for each
     * invoice.
     *
     * @param invoiceTable The TableView object to populate with invoice data
     * @param selectedFile The selected CSV file containing invoice data
     * @return An ArrayList of Invoice objects generated from the CSV file
     */
    private ArrayList<Invoice> populateTable(TableView<List<String>> invoiceTable, File selectedFile) {
        Parser pars = null;

        if (selectedFile == null) {
            showAlert("No File Selected", "Please select a CSV file before generating invoices.");
            return new ArrayList<>();
        }

        try {
            pars = new Parser(selectedFile);
        } catch (Exception e) {
            showAlert("Error", "There is a error with the csv file:" + "\n" + e.getMessage());
            return new ArrayList<>();
        }

        ArrayList<Invoice> invoices;
        try {
            invoices = pars.toInvoices(lm.getExtraItems(), lm.getForAll(), lm.getDiscounts());
        } catch (NullPointerException e) {
            showAlert("Error", "Missing invoice item lists. Please ensure you have added items and discounts.");
            return new ArrayList<>();
        }

        for (Invoice n : invoices) {
            StringBuilder items = new StringBuilder();
            double price = 0;
            ArrayList<InvoiceItem> excluded = lm.getForAll();

            for (InvoiceRow r : n.getInvoiceRows()) {
                price += r.getPrice();
                boolean isExcluded = false;
                if (r.getPrice() > 0) {
                    for (InvoiceItem i : excluded) {
                        if (i.articleNbr.equals(r.getArticleNumber())) {
                            isExcluded = true;
                            break;
                        }
                    }
                    if (!isExcluded) {
                        items.append(r.getArticleName()).append(", ");
                    }
                }
            }

            if (items.length() > 0) {
                items.setLength(items.length() - 2);
            }

            List<String> row = new ArrayList<>();
            row.add(n.getCustomerName());
            row.add(items.toString());
            row.add(String.format("%.2f", price));
            invoiceTable.getItems().add(row);
        }
        return invoices;
    }

    /**
     * Sends the generated invoices to Fortnox using the Fortnox API.
     * The method sends the invoices to Fortnox and displays a success message if
     * the operation is successful.
     * It also opens the Fortnox website in the default browser for the user to view
     * the invoices.
     *
     * @param invoices The ArrayList of Invoice objects to send to Fortnox
     */
    private void sendToFortnox(ArrayList<Invoice> invoices) {
        Api api = new Api();

        // Create and show loading dialog
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(40, 40);

        Alert loadingDialog = new Alert(Alert.AlertType.INFORMATION);
        loadingDialog.setTitle("Processing");
        loadingDialog.setHeaderText(null);
        loadingDialog.setContentText("Sending invoices to Fortnox...");
        loadingDialog.setGraphic(progressIndicator);

        // Thread to allow dialog to show while sending invoices
        Thread thread = new Thread(() -> {
            try {
                int result = api.sendInvoiceList(invoices);
                Platform.runLater(() -> {
                    loadingDialog.close();
                    if (result == invoices.size()) {
                        showAlert("Success",
                                "All invoices have been sent to Fortnox." + "\n" + result + " invoices sent.");
                        try {
                            Desktop.getDesktop().browse(new URI("https://fortnox.se"));
                        } catch (Exception e1) {
                            showAlert("Error", "An error occurred while opening Fortnox in your browser: " + "\n"
                                    + e1.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingDialog.close();
                    showAlert("Error", "An error occurred while sending invoices to Fortnox: " + "\n" + e.getMessage());
                });
                e.printStackTrace();
            }
        });

        thread.start();
        loadingDialog.show();

    }

    /**
     * Creates a scene for adding or editing an invoice item.
     * The scene contains input fields for the item name, price, and article number.
     * It also includes buttons for saving the item, removing the item (if editing),
     * and returning to the invoice items management scene.
     *
     * @param stage        The primary stage of the application
     * @param title        The title of the scene (Add Item or Edit Item)
     * @param item         The InvoiceItem object to be edited (null if adding a new
     *                     item)
     * @param isEdit       A boolean flag indicating if the item is being edited
     * @param saveCallback A Consumer function to save the item
     * @param backCallback A Runnable function to return to the invoice items
     *                     management scene
     * @return A Scene object configured for adding or editing an invoice item
     */
    private Scene createEditOrAddScene(Stage stage, String title, InvoiceItem item, boolean isEdit,
            Consumer<InvoiceItem> saveCallback, Runnable backCallback) {
        // Huvudlayout
        BorderPane window = new BorderPane();

        // Titel
        Label titleLabel = createLabel(title, "label");

        // Etiketter och textfält för "Name", "Price" och "Article Number"
        HBox nameRow = createInputRow("Name", isEdit ? item.key : "");
        HBox priceRow = createInputRow("Price", isEdit ? String.valueOf(item.price) : "");
        HBox articleNumberRow = createInputRow("Article Number", isEdit ? item.articleNbr : "");

        // Knappar
        Button backButton = createMenuButton("Back", stage, this::getDiscountScene);

        Button saveButton = createButton(isEdit ? "Save Changes" : "Add Item", "menu-button", e -> {
            try {
                String name = getTextFieldValue(nameRow);
                String articleNumber = getTextFieldValue(articleNumberRow);
                double price = Double.parseDouble(getTextFieldValue(priceRow));

                if (name.isEmpty() || articleNumber.isEmpty()) {
                    throw new IllegalArgumentException("Name and Article Number cannot be empty.");
                }

                InvoiceItem newItem = new InvoiceItem(name, articleNumber, price);
                saveCallback.accept(newItem);

                showAlert("Success", isEdit ? "Item has been updated" : "Item has been added");
                saveListManager(lm);
                backCallback.run();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid price.");
            } catch (IllegalArgumentException ex) {
                showAlert("Invalid Input", ex.getMessage());
            }
        });

        Button removeButton = null;
        if (isEdit) {
            removeButton = createButton("Remove", "remove-button", e -> {
                lm.remove(item);
                showAlert("Success", "Item has been removed");
                backCallback.run();
            });
        }

        // Knapp-layout
        HBox buttonLayout = createHBox(Pos.CENTER_LEFT, 10, backButton, saveButton);
        if (isEdit && removeButton != null) {
            buttonLayout.getChildren().add(removeButton);
        }

        // Kombinera allt i layouten
        VBox layout = createVBox(Pos.TOP_CENTER, 20, titleLabel, nameRow, priceRow, articleNumberRow, buttonLayout);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("vbox-container");

        window.setCenter(layout);

        // Skapa scenen
        Scene scene = new Scene(window, width, heigth);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    /**
     * Creates a CheckBox for toggling an invoice item for all invoices.
     * The CheckBox is used to include or exclude an item from all invoices.
     *
     * @param item The InvoiceItem object to toggle for all invoices
     * @return A CheckBox object for toggling the item for all invoices
     */
    private CheckBox createCheckBox(InvoiceItem item) {
        CheckBox checkBox = new CheckBox();

        checkBox.setOnAction(e -> lm.toggleForAll(item));
        return checkBox;
    }

    /**
     * Creates a Label with the specified text and style class.
     *
     * @param text       The text content of the label
     * @param styleClass The style class to apply to the label
     * @return A Label object with the specified text and style class
     */
    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    /**
     * Creates a Button with the specified text, style class, and action
     * 
     * @param text       The text content of the button
     * @param styleClass The style class to apply to the button
     * @param action     The action to perform when the button is clicked
     * @return Button object with the specified text, style class, and action
     * 
     */
    private Button createButton(String text, String styleClass,
            javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(action);
        return button;
    }

    /**
     * Creates a VBox with the specified alignment, spacing, and children nodes.
     * 
     * @param alignment The alignment of the VBox
     * @param spacing   The spacing between children nodes
     * @param children  The children nodes to add to the VBox
     * @return VBox object with the specified alignment, spacing, and children nodes
     */
    private VBox createVBox(Pos alignment, int spacing, javafx.scene.Node... children) {
        VBox box = new VBox(spacing, children);
        box.setAlignment(alignment);
        return box;
    }

    /**
     * Creates an HBox with the specified alignment, spacing, and children nodes.
     * 
     * @param alignment The alignment of the HBox
     * @param spacing   The spacing between children nodes
     * @param children  The children nodes to add to the HBox
     * @return HBox object with the specified alignment, spacing, and children nodes
     */
    private HBox createHBox(Pos alignment, int spacing, javafx.scene.Node... children) {
        HBox box = new HBox(spacing, children);
        box.setAlignment(alignment);
        return box;
    }

    /**
     * Creates a Text object with the specified content.
     * 
     * @param content The text content of the Text object
     * @return Text object with the specified content
     */
    private Text createText(String content) {
        Text text = new Text(content);
        text.getStyleClass().add("instructions-text");
        return text;
    }

    /**
     * Creates a VBox with a title and content for a tooltip.
     * 
     * @param title   The title of the tooltip
     * @param content The content of the tooltip
     * @return VBox object with the specified title and content
     */
    private VBox createTooltipBox(String title, String content) {
        return createVBox(Pos.TOP_CENTER, 10,
                createLabel(title, "label"),
                createText(content));
    }

    /**
     * Creates a menu button with the specified text, stage, and scene function.
     * 
     * @param text          The text content of the button
     * @param stage         The primary stage of the application
     * @param sceneFunction The function to generate the scene for the button
     * @return Button object with the specified text, stage, and scene function
     */
    private Button createMenuButton(String text, Stage stage, java.util.function.Function<Stage, Scene> sceneFunction) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setMinWidth(150);
        button.setOnAction(e -> stage.setScene(sceneFunction.apply(stage)));
        return button;
    }

    /**
     * Creates an input row with a label and text field for user input.
     * 
     * @param labelText    The text content of the label
     * @param initialValue The initial value of the text field
     * @return HBox object with a label and text field for user input
     */
    private HBox createInputRow(String labelText, String initialValue) {
        Label label = createLabel(labelText, "instructions-text");
        label.setMinWidth(120); // Håller etiketter justerade

        TextField textField = new TextField(initialValue);
        textField.getStyleClass().add("text-field");

        return createHBox(Pos.CENTER_LEFT, 10, label, textField);
    }

    /**
     * Gets the text content of a text field in an HBox.
     * 
     * @param row The HBox containing the text field
     * @return The text content of the text field
     */
    private String getTextFieldValue(HBox row) {
        for (Node node : row.getChildren()) {
            if (node instanceof TextField) {
                return ((TextField) node).getText();
            }
        }
        return "";
    }

    /**
     * Displays an alert dialog with the specified title and content.
     * 
     * @param title   The title of the alert dialog
     * @param content The content of the alert dialog
     * @return Alert object with the specified title and content
     */
    private Alert showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
        return alert;
    }

    /**
     * Creates a TableView for displaying invoice data.
     * 
     * @return TableView object for displaying invoice data
     */
    private TableView<List<String>> createInvoiceTable() {
        TableView<List<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<List<String>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(0)));

        TableColumn<List<String>, String> itemsColumn = new TableColumn<>("Items");
        itemsColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(1)));

        TableColumn<List<String>, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(2)));

        table.getColumns().addAll(nameColumn, itemsColumn, amountColumn);
        return table;
    }
}