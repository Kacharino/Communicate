package kacharino.communicate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MessengerApp extends Application {

    private static ChatClient client; // Reference to the client
    private static TextArea messageArea;
    private TextField inputField;
    private Button sendButton;

    public static void setClient(ChatClient clientInstance) {
        client = clientInstance;
    }

    public static void displayMessage(String message) {
        Platform.runLater(() -> {
            if (messageArea != null) {
                messageArea.appendText(message + "\n");
            } else {
                System.out.println("Error: messageArea is null. Cannot display message: " + message);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Messenger App");

        // Message Area Styling
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setFont(Font.font("Arial", 14));
        messageArea.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        messageArea.setWrapText(true);

        // Input Field Styling
        inputField = new TextField();
        inputField.setPromptText("Type a message...");
        inputField.setFont(Font.font("Arial", 14));
        inputField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        inputField.setOnAction(event -> sendMessage());

        // Send Button Styling
        sendButton = new Button("Send");
        sendButton.setFont(Font.font("Arial", 14));
        sendButton.setTextFill(Color.WHITE);
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        sendButton.setOnAction(event -> sendMessage());
        sendButton.setPrefHeight(30);

        // Layout Styling
        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #f1f1f1; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        HBox.setHgrow(inputField, Priority.ALWAYS); // Make inputField expand in width

        VBox root = new VBox(10, messageArea, inputBox);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #ffffff;");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 500, 600); // Slightly larger UI
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.trim().isEmpty()) {
            if (client != null) {
                client.sendMessage(message);
                inputField.clear();
            } else {
                displayMessage("Error: Not connected to the server!");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
