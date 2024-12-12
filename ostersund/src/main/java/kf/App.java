package kf;

import javafx.application.Application;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private Scene getAddItemScene(Stage stage) {
        return createEditOrAddScene(stage, "Add New Invoice Item", null, false,
            newItem -> lm.addInvoiceItem(newItem.key, newItem.articleNbr, newItem.price),
            () -> stage.setScene(getInvoiceItemsScene(stage))
        );
    }

    private Scene getInvoiceItemsScene(Stage stage) {
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));

        for (InvoiceItem item : lm.getInvoiceItems()) {
            HBox itemRow = createHBox(Pos.CENTER_LEFT, 10,
                    new CheckBox(),
                    createLabel(item.key, "instructions-text"),
                    createButton("Edit", "menu-button", e -> stage.setScene(getEditItemScene(stage, item))),
                    createButton("Remove", "remove-button", e -> {
                        lm.getInvoiceItems().remove(item);
                        stage.setScene(getInvoiceItemsScene(stage));
                    })
                
            );
            if (item.forAll) {
                ((CheckBox) itemRow.getChildren().get(0)).setSelected(true);
            }
            itemList.getChildren().add(itemRow);
        }

        VBox tooltipBox = createTooltipBox("Tool tip",
                "Remember to use the\nsame name for the item\nhere as it is declared in\nthe provided CSV file.");

        HBox buttonLayout = createHBox(Pos.CENTER, 20,
                createButton("Back", "menu-button", e -> {
                    saveListManager(lm);
                    stage.setScene(getMainLayout(stage));
                }),
                createButton("Add new Item", "menu-button", e -> stage.setScene(getAddItemScene(stage)))
        );

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
        return createEditOrAddScene(stage, "Edit Item", item, true,
            updatedItem -> lm.editItem(item, updatedItem.key, updatedItem.articleNbr, updatedItem.price),
            () -> stage.setScene(getInvoiceItemsScene(stage))
        );
    }
    private Scene getDiscountScene(Stage stage) {
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));

        for (InvoiceItem discount : lm.getDiscounts()) {
            HBox discountRow = createHBox(Pos.CENTER_LEFT, 10,
                    createLabel(discount.key, "instructions-text"),
                    createButton("Edit", "menu-button", e -> stage.setScene(getEditDiscountScene(stage, discount))),
                    createButton("Remove", "remove-button", e -> {
                        lm.removeDiscount(discount);
                        stage.setScene(getDiscountScene(stage));
                    })
            );
            itemList.getChildren().add(discountRow);
        }

        VBox tooltipBox = createTooltipBox("Tool tip",
                "Manage discounts here.\nUse names consistent\nwith your CSV file.");

        HBox buttonLayout = createHBox(Pos.CENTER, 20,
                createButton("Back", "menu-button", e -> {
                    saveListManager(lm);
                    stage.setScene(getMainLayout(stage));
                }),
                createButton("Add new Discount", "menu-button", e -> stage.setScene(getAddDiscountScene(stage)))
        );

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(30));
        layout.setLeft(new VBox(10, createLabel("Discounts", "instructions-text"), itemList));
        layout.setRight(tooltipBox);
        layout.setBottom(buttonLayout);

        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        return scene;
    }

    private Scene getEditDiscountScene(Stage stage, InvoiceItem discount) {
        return createEditOrAddScene(stage, "Edit Discount", discount, true,
            updatedDiscount -> lm.editDiscount(discount, updatedDiscount.key, updatedDiscount.articleNbr, updatedDiscount.price),
            () -> stage.setScene(getDiscountScene(stage))
        );
    }

    private Scene getAddDiscountScene(Stage stage) {
        return createEditOrAddScene(stage, "Add New Discount", null, false,
            newDiscount -> lm.addDiscount(newDiscount.key, newDiscount.articleNbr, newDiscount.price),
            () -> stage.setScene(getDiscountScene(stage))
        );
    }

    private ArrayList<Invoice> populateTable(TableView<List<String>> invoiceTable, File selectedFile) {
        if (selectedFile == null) {
            showAlert("No File Selected", "Please select a CSV file before generating invoices.");
            return new ArrayList<>();
        }

        Parser pars = new Parser(selectedFile);
        ArrayList<Invoice> invoices;
        try {
            invoices = pars.toInvoices(lm.getExtraItems(), lm.getForAll(), lm.getDiscounts());
        } catch (NullPointerException e) {
            showAlert("Error", "Missing invoice item lists. Please ensure data is properly loaded.");
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

    private void getApi(ArrayList<Invoice> invoices) {
        if (invoices.isEmpty()) {
            showAlert("No Invoices", "No invoices to send. Please generate invoices first.");
            return;
        }

        Api api = new Api();
        int sent = 0;
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new java.net.URI("https://apps.fortnox.se/fs/fs/login.php#"));
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            sent = api.sendInvoiceList(invoices);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        showAlert("Success", "Invoices sent to Fortnox: " + sent);
    }

    private void sendToFortnox(ArrayList<Invoice> invoices) {
        Api api = new Api();
        try {
            int sentCount = api.sendInvoiceList(invoices);
            System.out.println("Invoices sent to Fortnox: " + sentCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
    Scene scene = new Scene(window, 600, 400);
    scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    return scene;
}

    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Button createButton(String text, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(action);
        return button;
    }

    private VBox createVBox(Pos alignment, int spacing, javafx.scene.Node... children) {
        VBox box = new VBox(spacing, children);
        box.setAlignment(alignment);
        return box;
    }

    private HBox createHBox(Pos alignment, int spacing, javafx.scene.Node... children) {
        HBox box = new HBox(spacing, children);
        box.setAlignment(alignment);
        return box;
    }

    private Text createText(String content) {
        Text text = new Text(content);
        text.getStyleClass().add("instructions-text");
        return text;
    }

    private VBox createTooltipBox(String title, String content) {
        return createVBox(Pos.TOP_CENTER, 10,
                createLabel(title, "label"),
                createText(content)
        );
    }

    private Button createMenuButton(String text, Stage stage, java.util.function.Function<Stage, Scene> sceneFunction) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setMinWidth(150);
        button.setOnAction(e -> stage.setScene(sceneFunction.apply(stage)));
        return button;
    }

    private HBox createInputRow(String labelText, String initialValue) {
        Label label = createLabel(labelText, "instructions-text");
        label.setMinWidth(120); // Håller etiketter justerade
    
        TextField textField = new TextField(initialValue);
        textField.getStyleClass().add("text-field");
    
        return createHBox(Pos.CENTER_LEFT, 10, label, textField);
    }

    private String getTextFieldValue(HBox row) {
        for (Node node : row.getChildren()) {
            if (node instanceof TextField) {
                return ((TextField) node).getText();
            }
        }
        return "";
    }
    
    private TableView<List<String>> createInvoiceTable() {
        TableView<List<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
        TableColumn<List<String>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(0)));
    
        TableColumn<List<String>, String> itemsColumn = new TableColumn<>("Items");
        itemsColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(1)));
    
        TableColumn<List<String>, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(2)));
    
        table.getColumns().addAll(nameColumn, itemsColumn, amountColumn);
        return table;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}