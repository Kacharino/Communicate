package kacharino.communicate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MessengerApp extends Application {

    private static ChatClient client;
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
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Messenger App (Minimal)");

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setFont(Font.font("Arial", 14));
        messageArea.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Type command or message...");
        inputField.setOnAction(e -> sendMsg());

        sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMsg());

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        HBox.setHgrow(inputField, Priority.ALWAYS);

        VBox root = new VBox(10, messageArea, inputBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMsg() {
        String msg = inputField.getText();
        if (!msg.trim().isEmpty() && client != null) {
            client.sendMessage(msg);
            inputField.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}