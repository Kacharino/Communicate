package kacharino.communicate;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Haupt-Layout (vertikal)
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new javafx.geometry.Insets(10));

        // Chat-Bereich: Scrollable Text Area für Nachrichten
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(300);

        // Eingabebereich: Textfeld für die Nachricht
        TextField messageField = new TextField();
        messageField.setPromptText("Nachricht eingeben...");

        // Senden-Button
        Button sendButton = new Button("Senden");
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Methode zum Senden der Nachricht
        Runnable sendMessage = () -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                // Nachricht in den Chatbereich einfügen
                chatArea.appendText("Du: " + message + "\n");
                messageField.clear();  // Eingabefeld leeren
                chatArea.setScrollTop(Double.MAX_VALUE); // Scrollen zum neuesten Eintrag
            }
        };

        // Button-Click-Event
        sendButton.setOnAction(event -> sendMessage.run());

        // Enter-Key-Trigger
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage.run();
            }
        });

        // Layout für Eingabebereich und Button
        HBox inputLayout = new HBox(10, messageField, sendButton);
        inputLayout.setAlignment(Pos.CENTER);

        // Layout zusammenbauen
        mainLayout.getChildren().addAll(chatArea, inputLayout);

        // Scene und Stage erstellen
        Scene scene = new Scene(mainLayout, 400, 500);
        primaryStage.setTitle("Messenger");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}