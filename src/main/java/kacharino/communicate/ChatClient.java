package kacharino.communicate;

import javafx.application.Platform;

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

            // Pass the ChatClient instance to the MessengerApp
            MessengerApp.setClient(this);

            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> MessengerApp.displayMessage(finalMessage));
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void shutdown() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        Thread clientThread = new Thread(client);
        clientThread.start();

        MessengerApp.launch(MessengerApp.class, args);
    }
}
