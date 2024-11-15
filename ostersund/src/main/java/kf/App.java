
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
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Feature not yet implemented");
            alert.setContentText("Select a file when this feature is implemented.");
            alert.showAndWait();
        });

        Button invoiceItemsButton = new Button("Invoice Items");
        invoiceItemsButton.setMinWidth(150);
        invoiceItemsButton.getStyleClass().add("menu-button");

        invoiceItemsButton.setOnAction(e -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Feature not yet implemented");
            alert.setContentText("Sends you to a new scene when implemented.");
            alert.showAndWait();
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

        instructions.getChildren().addAll(howToUseLabel, instructionsText);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setLeft(buttonMenu);
        mainLayout.setRight(instructions);
        mainLayout.getStyleClass().add("scene");

        Scene scene = new Scene(mainLayout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        return scene;
    }
}