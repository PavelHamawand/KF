package kf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private File selectedFile;
    private ListManger lm = null;

    @Override
    public void start(Stage s) {
        s.setTitle("KF Öresund");
        s.setScene(getSelectFileScene(s));
        s.show();
        setListManager(this.lm);

    }

    private void setListManager(ListManger lm) {
        try (FileInputStream fileIn = new FileInputStream("listManger.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn)) {
            this.lm = (ListManger) in.readObject();
            System.out.println("Deserialized ListManger...");
        } catch (IOException i) {
            this.lm = new ListManger();
            System.out.println("Cant find listManger.ser, creating new ListManger...");
        } catch (ClassNotFoundException c) {
            System.out.println("ListManger class not found");
        }
    }

    private void saveListManager(ListManger lm) {

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

    private ArrayList<Invoice> populateTable(TableView<List<String>> invoiceTable, File selectedFile) {
        // parsa filen
        Parser pars = new Parser(selectedFile);
        ArrayList<Invoice> invoices = pars.toInvoices(lm.getExtraItems(), lm.getForAll(), lm.getDiscounts());

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
                items.setLength(items.length() - 2); // Remove the trailing comma and space
            }
        
           
            List<String> row = new ArrayList<>();
            row.add(n.getCustomerName());
            row.add(items.toString());
            row.add(String.valueOf(price));
            invoiceTable.getItems().add(row);
        };
        return invoices;
    }

    private void saveListManager(ListManger lm) {
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
        System.out.println("Invoices sent to Fortnox " + sent);
    }

    public Scene getSelectFileScene(Stage s) {
        Label titleLabel = new Label("Select your csv file");
        titleLabel.getStyleClass().add("label");

        Label instructionLabel = new Label("Please provide a correctly formatted csv");
        instructionLabel.getStyleClass().add("instructions-text");

        FileChooser file = new FileChooser();
        file.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Button selectFile = new Button("Browse files...");
        selectFile.getStyleClass().add("menu-button");

        selectFile.setOnAction(event -> {
            selectedFile = file.showOpenDialog(s);
            if (selectedFile != null) {
                stage.setScene(getMainLayout(stage));
            }
        });

        VBox layout = createVBox(Pos.CENTER, 10, titleLabel, instructionLabel, selectFileButton);
        layout.getStyleClass().add("vbox-container");

        Scene scene = new Scene(new StackPane(layout), 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    private Scene getMainLayout(Stage stage) {
        VBox buttonMenu = createVBox(Pos.CENTER_LEFT, 20,
                createLabel("Menu:", "label"),
                createMenuButton("Generate Invoice", stage, this::getInvoicesScene),
                createMenuButton("Invoice Items", stage, this::getInvoiceItemsScene),
                createMenuButton("Discounts", stage, this::getDiscountScene),
                createButton("Exit", "menu-button", e -> stage.close())
        );
        buttonMenu.setPadding(new Insets(30));

        VBox instructions = createVBox(Pos.TOP_LEFT, 10,
                createLabel("How to use:", "label"),
                createText(
                        "Generate Invoices:\nGenerates invoices based on your provided CSV file\n" +
                                "with the use of your active invoice items.\n\n" +
                                "Invoice Items:\nAdd the items you wish to have active\n" +
                                "for your invoice generation."
                ),
                createButton("Change csv file", "menu-button", e -> stage.setScene(getSelectFileScene(stage)))
        );
        instructions.setPadding(new Insets(30));

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setLeft(buttonMenu);
        mainLayout.setRight(instructions);

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

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
                buttonLayout
        );
        layout.setPadding(new Insets(30));
    
        // Returnera scenen med styling
        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    public Scene getInvoiceItemsScene(Stage s) {
        final ArrayList<InvoiceItem> items = lm.getInvoiceItems();
        // Title Label
        Label titleLabel = new Label("Invoice Items");
        titleLabel.getStyleClass().add("label");

        // Items Label
        Label itemsLabel = new Label("Items");
        itemsLabel.getStyleClass().add("instructions-text");

        // Table-like VBox for item checkboxes and "Edit" buttons
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));

        for (InvoiceItem item : items) {
            HBox itemRow = new HBox(10);

            // Checkbox
            CheckBox checkBox = new CheckBox();
            if (item.forAll) {
                checkBox.setSelected(true);
            }
            checkBox.setOnAction(e -> {
                lm.toggleForAll(item);
                saveListManager(lm);
            });

            // Item Name
            Label itemName = new Label(item.key);
            itemName.getStyleClass().add("instructions-text");

            // Edit Button
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> s.setScene(EditItem(s, item)));
            // editButton.getStyleClass().add("menu-button");
            editButton.setMinWidth(80);

            itemRow.getChildren().addAll(checkBox, itemName, editButton);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemList.getChildren().add(itemRow);
        }

        // Tooltip Section
        VBox tooltipBox = new VBox();
        tooltipBox.setPadding(new Insets(5, 10, 0, 10));
        tooltipBox.setAlignment(Pos.TOP_CENTER);

        Label tooltipTitle = new Label("Tool tip");
        tooltipTitle.getStyleClass().add("label");

        Text tooltipText = new Text(
                "Remember to use the\n same name for the item\n here as it is declared in\n the provided CSV file.");

        // tooltipText.setWrapText(true);
        tooltipText.getStyleClass().add("instructions-text");

        tooltipBox.getChildren().addAll(tooltipTitle, tooltipText);
        tooltipBox.getStyleClass().add("tooltip-box");

        // Buttons
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(100);
        backButton.setOnAction(e -> {
            s.setScene(getMainLayout(s)); // Go back to the main menu
            saveListManager(lm);
        });

        Button addItemButton = new Button("Add new Item");
        addItemButton.getStyleClass().add("menu-button");
        addItemButton.setMinWidth(150);
        addItemButton.setOnAction(e -> {
            s.setScene(AddNewItem(s));
        });

        // Button Layout
        HBox buttonLayout = new HBox(20, backButton, addItemButton);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.setPadding(new Insets(20));

        // Combine everything into the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30));
        mainLayout.setLeft(new VBox(10, createLabel("Items", "instructions-text"), itemList));
        mainLayout.setRight(tooltipBox);
        mainLayout.setBottom(buttonLayout);

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    private Scene getEditItemScene(Stage stage, InvoiceItem item) {
        Label titleLabel = createLabel("Edit Invoice Item", "label");

        TextField nameField = new TextField(item.key);
        TextField priceField = new TextField(String.valueOf(item.price));
        TextField articleNumberField = new TextField(item.articleNbr);

        VBox inputFields = createVBox(Pos.TOP_CENTER, 10,
                createLabel("Name:", "instructions-text"), nameField,
                createLabel("Price:", "instructions-text"), priceField,
                createLabel("Article Number:", "instructions-text"), articleNumberField
        );

        Button saveButton = createButton("Save", "menu-button", e -> {
            item.key = nameField.getText();
            item.price = Double.parseDouble(priceField.getText());
            item.articleNbr = articleNumberField.getText();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Item Updated");
            alert.setHeaderText(null);
            alert.setContentText("The item has been updated successfully.");
            alert.showAndWait();

            stage.setScene(getInvoiceItemsScene(stage));
        });

        Button addItem = new Button("Add");
        addItem.getStyleClass().add("menu-button");
        addItem.setMinWidth(150);
        addItem.setOnAction(e -> {

            try {
                lm.addInvoiceItem(name.getText(), articleNumber.getText(), Double.parseDouble(price.getText()));
            } catch (Exception e1) {
                Alert added = new Alert(AlertType.ERROR);
                added.setContentText(e1.getMessage());
                added.showAndWait();
            }

            Alert added = new Alert(AlertType.INFORMATION);
            added.setContentText("Item has been added");
            added.showAndWait();
            saveListManager(lm);
            s.setScene(getInvoiceItemsScene(s));
        });

        buttonMeny.getChildren().addAll(backButton, addItem);

        grid.getChildren().addAll(add, labels, TextFields, buttonMeny);
        grid.getStyleClass().add("vbox-container");

        window.setCenter(grid);

        Scene scene = new Scene(window, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    public Scene EditItem(Stage s, InvoiceItem item) {

        BorderPane window = new BorderPane();
        VBox grid = new VBox(10);
        grid.setPadding(new Insets(500, 10, 5, 10));
        HBox labels = new HBox(125);
        HBox TextFields = new HBox(10);
        HBox buttonMenu = new HBox(10);

        Label edit = new Label("Edit Item");
        edit.getStyleClass().add("label");
        edit.setAlignment(Pos.CENTER);

        Label nameToTextField = new Label("Name");
        Label priceToTextField = new Label("Price");
        Label articleNumberToTextField = new Label("Article Number");

        labels.getChildren().addAll(nameToTextField, priceToTextField, articleNumberToTextField);

        TextField name = new TextField(item.key);
        TextField price = new TextField(String.valueOf(item.price));
        TextField articleNumber = new TextField(item.articleNbr);

        TextFields.getChildren().addAll(name, price, articleNumber);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(100);
        backButton.setMaxWidth(150);
        backButton.setOnAction(e -> {
            s.setScene(getInvoiceItemsScene(s));
        });

        Button addItem = new Button("Add the edited item");
        addItem.getStyleClass().add("menu-button");
        addItem.setMinWidth(100);
        addItem.setMaxWidth(150);
        addItem.setOnAction(e -> {

            lm.editItem(item, name.getText(), articleNumber.getText(), Double.parseDouble(price.getText()));

            Alert edited = new Alert(AlertType.INFORMATION);
            edited.setContentText("Item has been updated");
            edited.showAndWait();
            saveListManager(lm);
            s.setScene(getInvoiceItemsScene(s));
        });

        // Lägg till Remove-knappen
        Button removeItemButton = new Button("Remove");
        removeItemButton.getStyleClass().add("remove-button"); // CSS klass för röd knapp
        removeItemButton.setMinWidth(100);
        removeItemButton.setMaxWidth(150);

        removeItemButton.setOnAction(e -> {
            lm.remove(item);
            Alert removed = new Alert(AlertType.INFORMATION);
            removed.setContentText("Item has been removed");
            removed.showAndWait();
            s.setScene(getInvoiceItemsScene(s)); // Tillbaka till huvudlistan
        });

        // Lägg till alla knappar i HBox
        buttonMenu.getChildren().addAll(backButton, addItem, removeItemButton);
        buttonMenu.setAlignment(Pos.CENTER_LEFT);

        grid.getChildren().addAll(edit, labels, TextFields, buttonMenu);
        grid.getStyleClass().add("vbox-container");

        window.setCenter(grid);

        Scene scene = new Scene(window, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    public Scene getDiscountScene(Stage s) {
        final ArrayList<InvoiceItem> discounts = lm.getDiscounts();
        // Title Label
        Label titleLabel = new Label("Discounts");
        titleLabel.getStyleClass().add("label");

        // Items Label
        Label itemsLabel = new Label("Items");
        itemsLabel.getStyleClass().add("instructions-text");

        // Table-like VBox for item checkboxes and "Edit" buttons
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));

        for (InvoiceItem item : discounts) {
            HBox itemRow = new HBox(10);

            // Item Name
            Label itemName = new Label(item.key);
            itemName.getStyleClass().add("instructions-text");

            // Edit Button
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> s.setScene(EditDiscountItem(s, item)));
            // editButton.getStyleClass().add("menu-button");
            editButton.setMinWidth(80);

            itemRow.getChildren().addAll(itemName, editButton);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemList.getChildren().add(itemRow);
        }

        // Tooltip Section
        VBox tooltipBox = new VBox();
        tooltipBox.setPadding(new Insets(5, 10, 0, 10));
        tooltipBox.setAlignment(Pos.TOP_CENTER);

        Label tooltipTitle = new Label("Tool tip");
        tooltipTitle.getStyleClass().add("label");

        Text tooltipText = new Text(
                "Remember to use the\n same name for the item\n here as it is declared in\n the provided CSV file.");

        // tooltipText.setWrapText(true);
        tooltipText.getStyleClass().add("instructions-text");

        tooltipBox.getChildren().addAll(tooltipTitle, tooltipText);
        tooltipBox.getStyleClass().add("tooltip-box");

        // Buttons
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(100);
        backButton.setOnAction(e -> {
            saveListManager(lm);
            s.setScene(getMainLayout(s)); // Go back to the main menu
        });

        Button addItemButton = new Button("Add new Item");
        addItemButton.getStyleClass().add("menu-button");
        addItemButton.setMinWidth(150);
        addItemButton.setOnAction(e -> {
            s.setScene(AddNewDiscount(s));
        });

        // Button Layout
        HBox buttonLayout = new HBox(20, backButton, addItemButton);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.setPadding(new Insets(20));

        // Combine everything into the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30));
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 20, 20));

        mainLayout.setLeft(new VBox(10, itemsLabel, itemList));
        mainLayout.setRight(tooltipBox);
        mainLayout.setBottom(buttonLayout);
        mainLayout.getStyleClass().add("scene");

        // Scene
        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;
    }

    public Scene AddNewDiscount(Stage s) {

        BorderPane window = new BorderPane();
        VBox grid = new VBox(10);
        grid.setPadding(new Insets(500, 10, 5, 10));
        HBox labels = new HBox(115);
        HBox TextFields = new HBox(10);
        HBox buttonMeny = new HBox(100);

        Label add = new Label("Add New Item");
        add.getStyleClass().add("label");
        add.setAlignment(Pos.CENTER);

        Label nameToTextField = new Label("Name");
        Label AmountToTextField = new Label("Amount");
        Label articleNumberToTextField = new Label("Article Number");

        labels.getChildren().addAll(nameToTextField, AmountToTextField, articleNumberToTextField);

        TextField name = new TextField();
        TextField amount = new TextField();
        TextField articleNumber = new TextField();

        TextFields.getChildren().addAll(name, amount, articleNumber);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(150);
        backButton.setOnAction(e -> {
            s.setScene(getDiscountScene(s));
        });

        Button addItem = new Button("Add discount item");
        addItem.getStyleClass().add("menu-button");
        addItem.setMinWidth(150);
        addItem.setOnAction(e -> {

            lm.addDiscount(name.getText(), articleNumber.getText(), Double.parseDouble(amount.getText()));

            Alert added = new Alert(AlertType.INFORMATION);
            added.setContentText("Item has been added");
            added.showAndWait();
            s.setScene(getDiscountScene(s));
        });

        buttonMeny.getChildren().addAll(backButton, addItem);

        grid.getChildren().addAll(add, labels, TextFields, buttonMeny);
        grid.getStyleClass().add("vbox-container");

        window.setCenter(grid);

        Scene scene = new Scene(window, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    public Scene EditDiscountItem(Stage s, InvoiceItem discount) {

        BorderPane window = new BorderPane();
        VBox grid = new VBox(10);
        grid.setPadding(new Insets(500, 10, 5, 10));
        HBox labels = new HBox(115);
        HBox TextFields = new HBox(10);
        HBox buttonMeny = new HBox(10);
        buttonMeny.setAlignment(Pos.CENTER_LEFT);

        Label edit = new Label("Edit discount");
        edit.getStyleClass().add("label");
        edit.setAlignment(Pos.CENTER);

        Label nameToTextField = new Label("Name");
        Label amountToTextField = new Label("Amount");
        Label articleNumberToTextField = new Label("Article Number");

        labels.getChildren().addAll(nameToTextField, amountToTextField, articleNumberToTextField);

        TextField name = new TextField(discount.key);
        TextField amount = new TextField(discount.price + "");
        TextField articleNumber = new TextField(discount.articleNbr);

        TextFields.getChildren().addAll(name, amount, articleNumber);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(100);
        backButton.setMaxWidth(150);
        backButton.setOnAction(e -> {
            s.setScene(getDiscountScene(s));
        });

        Button addDiscountItem = new Button("Add the edited discount item");
        addDiscountItem.getStyleClass().add("menu-button");
        addDiscountItem.setMinWidth(100);
        addDiscountItem.setMaxWidth(150);
        addDiscountItem.setOnAction(e -> {

            lm.editDiscount(discount, name.getText(), articleNumber.getText(), Double.parseDouble(amount.getText()));

            Alert edited = new Alert(AlertType.INFORMATION);
            edited.setContentText("Item has been updated");
            edited.showAndWait();
            s.setScene(getDiscountScene(s));
        });

        // Lägg till Remove-knappen
        Button removeDiscountButton = new Button("Remove");
        removeDiscountButton.getStyleClass().add("remove-button"); // CSS klass för röd knapp
        removeDiscountButton.setMinWidth(100);
        removeDiscountButton.setMaxWidth(150);

        removeDiscountButton.setOnAction(e -> {
            lm.removeDiscount(discount);
            Alert removed = new Alert(AlertType.INFORMATION);
            removed.setContentText("Discount item has been removed");
            removed.showAndWait();
            s.setScene(getDiscountScene(s)); // Tillbaka till huvudlistan
        });

        // Lägg till alla knappar i HBox
        buttonMeny.getChildren().addAll(backButton, addDiscountItem, removeDiscountButton);

        grid.getChildren().addAll(edit, labels, TextFields, buttonMeny);
        grid.getStyleClass().add("vbox-container");

        window.setCenter(grid);

        Scene scene = new Scene(window, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

}