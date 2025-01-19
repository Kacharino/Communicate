package kacharino.communicate;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Der ChatClient stellt eine Verbindung zum Server her und empfängt fortlaufend
 * Nachrichten, die anschließend an die MessengerApp (JavaFX-GUI) weitergeleitet werden.
 * <p>
 * Standardmäßig wird versucht, sich mit der IP 127.0.0.1:9696 zu verbinden.
 */
public class ChatClient implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Hauptmethode des Clients: Stellt eine Verbindung zum Server her und
     * lauscht auf eingehende Nachrichten. Eingehende Texte werden
     * automatisch an die MessengerApp weitergeleitet.
     * <p>
     * Im Fehlerfall (z. B. Verbindung nicht möglich) wird die Methode {@link #shutdown()} aufgerufen.
     */
    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9696);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Informiert die MessengerApp, dass dieser Client bereit ist
            Platform.runLater(() -> MessengerApp.setClient(this));

            // Liest fortlaufend Meldungen vom Server
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message;
                // Weiterleitung an die GUI im JavaFX-Thread
                Platform.runLater(() -> MessengerApp.displayMessage(finalMessage));
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    /**
     * Sendet eine Nachricht an den Server.
     *
     * @param message der zu sendende Text
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Schließt die Verbindung zum Server und gibt alle Ressourcen frei.
     */
    public void shutdown() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            // Fehlermeldung unterdrückt
        }
    }

    /**
     * Startet die JavaFX-Anwendung sowie den ChatClient in einem eigenen Thread.
     * Hiermit lässt sich der Client eigenständig (ohne separate GUI) testen.
     *
     * @param args nicht genutzt
     */
    public static void main(String[] args) {
        // Initialisiert den JavaFX-Thread
        Platform.startup(() -> {
            Stage primaryStage = new Stage();
            MessengerApp messengerApp = new MessengerApp();
            try {
                messengerApp.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Erzeugt und startet den ChatClient
            ChatClient client = new ChatClient();
            Thread clientThread = new Thread(client);
            clientThread.start();
        });
    }
}
