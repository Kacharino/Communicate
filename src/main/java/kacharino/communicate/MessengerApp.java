package kacharino.communicate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kacharino.communicate.ChatClient;

public class MessengerApp extends Application {

    private static ChatClient client; // Reference to the client
    private static TextArea messageArea;
    private TextField inputField;
    private Button sendButton;
    private Button newClientButton;

    public static void setClient(ChatClient clientInstance) {
        client = clientInstance;
    }

    public static void displayMessage(String message) {
        if (messageArea != null) {
            Platform.runLater(() -> messageArea.appendText(message + "\n"));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Messenger App");

        // Set up UI components
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefHeight(400);

        inputField = new TextField();
        inputField.setPromptText("Type a message...");
        inputField.setPrefHeight(30);

        sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage());

        newClientButton = new Button("Open New Client");
        newClientButton.setOnAction(event -> openNewClientWindow());

        VBox messageBox = new VBox(10, messageArea);
        messageBox.setPrefHeight(500);

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, messageBox, inputBox, newClientButton);
        root.setPadding(new javafx.geometry.Insets(20));

        Scene scene = new Scene(root, 400, 600);
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

    private void openNewClientWindow() {
        // Create a new client
        ChatClient newClient = new ChatClient();
        Thread newClientThread = new Thread(newClient);
        newClientThread.start();

        // Create a new stage for the new client
        MessengerApp newMessengerApp = new MessengerApp();
        newMessengerApp.setClient(newClient);

        // Launch the new stage in a new window
        Stage newStage = new Stage();
        newMessengerApp.start(newStage); // Start the new client in a new window
    }

    public static void main(String[] args) {
        launch(args);
    }
}
