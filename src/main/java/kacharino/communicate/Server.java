package kacharino.communicate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private final File chatHistoryFile;

    public Server() {
        connections = new ArrayList<>();
        done = false;
        chatHistoryFile = new File("chat_history.txt"); // File to store chat history
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9696);
            ExecutorService pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        // Append message to chat history file
        saveMessageToFile(message);

        // Broadcast the message to all clients
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    private void saveMessageToFile(String message) {
        try (FileWriter writer = new FileWriter(chatHistoryFile, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Error saving message to file: " + e.getMessage());
        }
    }

    private String loadChatHistory() {
        if (!chatHistoryFile.exists()) {
            return ""; // No chat history yet
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(chatHistoryFile))) {
            StringBuilder history = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                history.append(line).append("\n");
            }
            return history.toString();
        } catch (IOException e) {
            System.err.println("Error reading chat history: " + e.getMessage());
            return "";
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class ConnectionHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Send chat history to the client
                out.println("=== Chat History ===");
                out.println(loadChatHistory());
                out.println("====================");

                // Prompt for a nickname
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                if (nickname == null || nickname.trim().isEmpty()) {
                    nickname = "Guest";
                }
                broadcast(nickname + " joined the chat!");

                // Handle incoming messages
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                connections.remove(this);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
