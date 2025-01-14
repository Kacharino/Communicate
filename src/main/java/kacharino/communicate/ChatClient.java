package kacharino.communicate;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9696);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Geben wir dem MessengerApp Bescheid
            Platform.runLater(() -> MessengerApp.setClient(this));

            // Lies fortlaufend vom Server:
            String message;
            while ((message = in.readLine()) != null) {
                final String finalMessage = message;
                Platform.runLater(() -> MessengerApp.displayMessage(finalMessage));
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    public void shutdown() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static void main(String[] args) {
        Platform.startup(() -> {
            Stage primaryStage = new Stage();
            MessengerApp messengerApp = new MessengerApp();
            try {
                messengerApp.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ChatClient client = new ChatClient();
            Thread clientThread = new Thread(client);
            clientThread.start();
        });
    }
}