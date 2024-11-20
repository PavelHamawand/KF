
package kf;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {
    private File selectedFile;

    @Override
    public void start(Stage s) {

        s.setTitle("KF Öresund");
        s.setScene(getSelectFileScene(s));
        s.show();

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
                System.out.println("File selected: " + selectedFile.getAbsolutePath());
                s.setScene(getMainLayout(s));
                s.show();
            }
        });

        VBox box = new VBox(10, titleLabel, instructionLabel, selectFile);
        box.getStyleClass().add("vbox-container");
        box.setAlignment(Pos.CENTER);

        StackPane p = new StackPane(box);
        p.getStyleClass().add("scene");

        Scene scene = new Scene(p, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;
    }

    public Scene getMainLayout(Stage s) {

        VBox buttonMenu = new VBox(20);
        buttonMenu.setPadding(new Insets(30));
        buttonMenu.setAlignment(Pos.CENTER_LEFT);

        Label menuLabel = new Label("Menu:");
        menuLabel.getStyleClass().add("label");

        Button generateInvoiceButton = new Button("Generate Invoice");
        generateInvoiceButton.setMinWidth(150);
        generateInvoiceButton.getStyleClass().add("menu-button");

        generateInvoiceButton.setOnAction(e -> {
            s.setScene(getInvoicesScene(s)); // Switch to invoices scene
            s.show();
        });

        Button invoiceItemsButton = new Button("Invoice Items");
        invoiceItemsButton.setMinWidth(150);
        invoiceItemsButton.getStyleClass().add("menu-button");

        invoiceItemsButton.setOnAction(e -> {
            s.setScene(getInvoiceItemsScene(s)); // Switch to invoices scene
            s.show();
        });

        Button exitButton = new Button("Exit");
        exitButton.setMinWidth(150);
        exitButton.getStyleClass().add("menu-button");
        exitButton.setOnAction(e -> s.close());

        buttonMenu.getChildren().addAll(menuLabel, generateInvoiceButton, invoiceItemsButton, exitButton);

        VBox instructions = new VBox(10);
        instructions.setPadding(new Insets(30));
        instructions.setAlignment(Pos.CENTER_LEFT);

        Label howToUseLabel = new Label("How to use:");
        howToUseLabel.getStyleClass().add("label");

        Text instructionsText = new Text(
                "Generate Invoices:\n" +
                        "Generates invoices based on your provided CSV file\n" +
                        "with the use of your active invoice items.\n\n" +
                        "Invoice Items:\n" +
                        "Add the items you wish to have active\n" +
                        "for your invoice generation.");
        instructionsText.getStyleClass().add("instructions-text");

        Button changeFile = new Button("Change csv file");
        changeFile.setMinWidth(150);
        changeFile.getStyleClass().add("menu-button");
        changeFile.setOnAction(e -> {
            s.setScene(getSelectFileScene(s));
            s.show();
        });

        instructions.getChildren().addAll(howToUseLabel, instructionsText, changeFile);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setLeft(buttonMenu);
        mainLayout.setRight(instructions);
        mainLayout.getStyleClass().add("scene");

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;
    }

    public Scene getInvoicesScene(Stage s) {

        Label titleLabel = new Label("Generate invoices");
        titleLabel.getStyleClass().add("label");

        Label invoicesLabel = new Label("Invoices");
        invoicesLabel.getStyleClass().add("instructions-text"); // Smaller font style
        invoicesLabel.setAlignment(Pos.TOP_LEFT); // Align to the left

        // skapa tableview
        TableView<List<String>> invoiceTable = new TableView<>();
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        invoiceTable.setMinWidth(400);
        invoiceTable.setMaxHeight(150);

        // definiera olika kolumner
        TableColumn<List<String>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(0)));

        TableColumn<List<String>, String> itemsColumn = new TableColumn<>("Items");
        itemsColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(1)));

        TableColumn<List<String>, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(2)));

        // lägg till kolumner i tabell
        invoiceTable.getColumns().addAll(nameColumn, itemsColumn, amountColumn);

        // hittepå ba
        invoiceTable.getItems().addAll(
                List.of("Anders Persson", "Kajakplats", "1000kr"),
                List.of("Albin Bernier", "Kajakplats, Utökat träningskort", "1600kr"));

        Button backButton = new Button("Back");
        backButton.setMinWidth(100);
        backButton.getStyleClass().add("menu-button");

        backButton.setOnAction(e -> {
            s.setScene(getMainLayout(s)); // Switch to invoices scene
            s.show();
        });

        Button sendButton = new Button("Send to Fortnox");
        sendButton.setMinWidth(150);
        sendButton.getStyleClass().add("menu-button");

        // Horisontell layout för knapparna
        HBox buttonLayout = new HBox(20, backButton, sendButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // Layout
        VBox layout = new VBox(10, titleLabel, invoicesLabel, invoiceTable, buttonLayout);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(30));
        layout.getStyleClass().add("scene"); // Apply the background color from .scene

        // Align "Invoices" label to the left
        VBox.setMargin(invoicesLabel, new Insets(0, 0, 0, 20)); // Add left margin

        // skapa o returnera scen
        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;
    }

    public Scene getInvoiceItemsScene(Stage s) {
        // Title Label
        Label titleLabel = new Label("Invoice Items");
        titleLabel.getStyleClass().add("label");
    
        // Items Label
        Label itemsLabel = new Label("Items");
        itemsLabel.getStyleClass().add("instructions-text");
    
        // Table-like VBox for item checkboxes and "Edit" buttons
        VBox itemList = new VBox(10);
        itemList.setPadding(new Insets(20));
        
        // Mock Data for Items
        String[] items = {"Medlemskap", "Träningskort", "Kajakplats", "Utökat träningskort"};
        for (String item : items) {
            HBox itemRow = new HBox(10);
    
            // Checkbox
            CheckBox checkBox = new CheckBox();
            // checkBox.getStyleClass().add("menu-button"); // Optional: Add consistent style
    
            // Item Name
            Label itemName = new Label(item);
            itemName.getStyleClass().add("instructions-text");
    
            // Edit Button
            Button editButton = new Button("Edit");
            // editButton.getStyleClass().add("menu-button");
            editButton.setMinWidth(80);
    
            itemRow.getChildren().addAll(checkBox, itemName, editButton);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemList.getChildren().add(itemRow);
        }
    
        // Tooltip Section
        VBox tooltipBox = new VBox();
        tooltipBox.setPadding(new Insets(5,10,0,10));
        tooltipBox.setAlignment(Pos.TOP_CENTER);
        tooltipBox.setStyle("-fx-border-color: blue; -fx-border-radius: 5px");
    
        Label tooltipTitle = new Label("Tool tip");
        tooltipTitle.getStyleClass().add("label");
    
        Text tooltipText = new Text(
            "Remember to use the\n same name for the item\n here as it is declared in\n the provided CSV file."
        );

       // tooltipText.setWrapText(true);
        tooltipText.getStyleClass().add("instructions-text");
    
        tooltipBox.getChildren().addAll(tooltipTitle, tooltipText);
    
        // Buttons
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setMinWidth(100);
        backButton.setOnAction(e -> {
            s.setScene(getMainLayout(s)); // Go back to the main menu
        });
    
        Button addItemButton = new Button("Add new Item");
        addItemButton.getStyleClass().add("menu-button");
        addItemButton.setMinWidth(150);
    
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
    
        // Scene
        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    
        return scene;
    }
}