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

/**
 * Die MessengerApp ist eine einfache JavaFX-Anwendung, die als Frontend
 * für den ChatClient dient. Sie zeigt empfangene Nachrichten in einer TextArea an
 * und erlaubt das Versenden von Eingaben an den Server.
 */
public class MessengerApp extends Application {

    /**
     * Statische Referenz auf den ChatClient, damit die App Nachrichten
     * an den Server senden kann. Wird beim Starten des Clients gesetzt.
     */
    private static ChatClient client;

    /**
     * TextArea, in der sämtliche eingehenden Nachrichten angezeigt werden.
     */
    private static TextArea messageArea;

    /**
     * Textfeld zur Eingabe von Nachrichten oder Befehlen.
     */
    private TextField inputField;

    /**
     * Button, um alternativ zum Drücken der Enter-Taste eine Nachricht abzusenden.
     */
    private Button sendButton;

    /**
     * Setzt den extern erzeugten ChatClient. Sobald dieser verfügbar ist,
     * kann die App Nachrichten an den Server schicken.
     *
     * @param clientInstance Referenz auf den laufenden ChatClient
     */
    public static void setClient(ChatClient clientInstance) {
        client = clientInstance;
    }

    /**
     * Zeigt eine empfangene Nachricht in der TextArea an. Wird vom Client-Thread
     * aufgerufen und im JavaFX-Thread ausgeführt.
     *
     * @param message die anzuzeigende Nachricht
     */
    public static void displayMessage(String message) {
        Platform.runLater(() -> {
            if (messageArea != null) {
                messageArea.appendText(message + "\n");
            }
        });
    }

    /**
     * Startet die JavaFX-GUI. Hier wird das Fenster aufgebaut und angezeigt.
     *
     * @param primaryStage das Hauptfenster der Anwendung
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Messenger App");

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

    /**
     * Wird aufgerufen, sobald der Nutzer eine Nachricht über das Eingabefeld
     * oder den Send-Button absenden möchte.
     */
    private void sendMsg() {
        String msg = inputField.getText();
        if (!msg.trim().isEmpty() && client != null) {
            client.sendMessage(msg);
            inputField.clear();
        }
    }

    /**
     * Einstiegspunkt der JavaFX-Anwendung. Ruft intern die Methode {@link #launch(String[])} auf.
     *
     * @param args nicht genutzt
     */
    public static void main(String[] args) {
        launch(args);
    }
}
