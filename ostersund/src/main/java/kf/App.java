
package kf;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage s) {
        s.setTitle("KF Öresund");
        s.setScene(getMainLayout(s, null));
        s.show();
    }

    public Scene getMainLayout(Stage s, File choosedFile) {

        // Create VBox for the left menu with buttons
        VBox buttonMenu = new VBox(20);
        buttonMenu.setPadding(new Insets(30));
        buttonMenu.setAlignment(Pos.CENTER_LEFT);

        // Menu label
        Label menuLabel = new Label("Menu:");
        menuLabel.getStyleClass().add("label");

        // Generate Invoice Button
        Button generateInvoiceButton = new Button("Generate Invoice");
        generateInvoiceButton.setMinWidth(150);
        generateInvoiceButton.getStyleClass().add("menu-button");

        generateInvoiceButton.setOnAction(e -> {
            // Display an alert for selecting file
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Feature not yet implemented");
            alert.setContentText("Select a file when this feature is implemented.");
            alert.showAndWait();
        });

        // Invoice Items Button
        Button invoiceItemsButton = new Button("Invoice Items");
        invoiceItemsButton.setMinWidth(150);
        invoiceItemsButton.getStyleClass().add("menu-button");

        invoiceItemsButton.setOnAction(e -> {
            // Display an alert for selecting file
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Feature not yet implemented");
            alert.setContentText("Sends you to a new scene when implemented.");
            alert.showAndWait();
        });

        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setMinWidth(150);
        exitButton.getStyleClass().add("menu-button");
        exitButton.setOnAction(e -> s.close());

        // Add buttons to the VBox
        buttonMenu.getChildren().addAll(menuLabel, generateInvoiceButton, invoiceItemsButton, exitButton);

        // Instruction Text on the right
        VBox instructions = new VBox(10);
        instructions.setPadding(new Insets(30));
        instructions.setAlignment(Pos.TOP_LEFT);

        // "How to use" section label
        Label howToUseLabel = new Label("How to use:");
        howToUseLabel.getStyleClass().add("label");

        // Instruction details
        Text instructionsText = new Text(
                "Generate Invoices:\n" +
                        "Generates invoices based on your provided CSV file\n" +
                        "with the use of your active invoice items.\n\n" +
                        "Invoice Items:\n" +
                        "Add the items you wish to have active\n" +
                        "for your invoice generation.");
        instructionsText.getStyleClass().add("instructions-text");

        // Add "How to use" label and text to the instructions VBox
        instructions.getChildren().addAll(howToUseLabel, instructionsText);

        // BorderPane layout to organize the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setLeft(buttonMenu);
        mainLayout.setCenter(instructions);

        // Load and apply the CSS file
        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;

    }

}
