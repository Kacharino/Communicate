package kacharino.communicate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {

    private final ServerSocket server;
    private boolean isRunning;

    private final List<ConnectionHandler> connections;
    private final Map<String, ConnectionHandler> userMap;   // <--- NEU: username -> handler

    private final File chatHistoryFile;
    private final UserManager userManager;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.isRunning = true;
        this.connections = new CopyOnWriteArrayList<>();
        this.userMap = new ConcurrentHashMap<>();  // Thread-sicher
        this.chatHistoryFile = new File("chat_history.txt");
        this.userManager = new UserManager("users.txt");
    }

    @Override
    public void run() {
        System.out.println("Server started on port " + server.getLocalPort());
        while (isRunning) {
            try {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                new Thread(handler).start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Broadcast an alle (öffentliche Nachricht).
     */
    public synchronized void broadcast(String message) {
        saveMessageToFile(message);
        for (ConnectionHandler ch : connections) {
            ch.sendMessage(message);
        }
    }

    /**
     * Direkte Nachricht an einen bestimmten User (falls online).
     * Speichern wir ebenfalls in derselben History-Datei.
     */
    public synchronized void sendDirectMessage(String fromUser, String toUser, String msg) {
        ConnectionHandler targetHandler = userMap.get(toUser);
        ConnectionHandler fromHandler = userMap.get(fromUser);

        String directMsg = "[DM] " + fromUser + " -> " + toUser + ": " + msg;
        // In Datei speichern
        saveMessageToFile(directMsg);

        if (targetHandler != null) {
            // An Empfänger schicken
            targetHandler.sendMessage(directMsg);
        } else {
            // Empfänger nicht eingeloggt
            if (fromHandler != null) {
                fromHandler.sendMessage("User '" + toUser + "' not found or not logged in.");
            }
        }

        // Optional: auch an den Sender schicken, damit er sieht, was er verschickt hat.
        if (fromHandler != null) {
            fromHandler.sendMessage(directMsg);
        }
    }

    private void saveMessageToFile(String message) {
        try (FileWriter fw = new FileWriter(chatHistoryFile, true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    private String loadChatHistory() {
        if (!chatHistoryFile.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(chatHistoryFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading chat history: " + e.getMessage());
        }
        return sb.toString();
    }

    public void shutdown() {
        isRunning = false;
        try {
            server.close();
            for (ConnectionHandler ch : connections) {
                ch.closeConnection();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    // =========================================================
    // =============== INNER CLASS: ConnectionHandler ==========
    // =========================================================
    private class ConnectionHandler implements Runnable {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean loggedIn;
        private String username;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
            this.loggedIn = false;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Welcome to the Chat Server!");
                out.println("Use: /login <user> <pass> or /register <user> <pass>");

                // Login-/Register-Schleife
                while (!loggedIn) {
                    String line = in.readLine();
                    if (line == null) {
                        closeConnection();
                        return;
                    }
                    handleLoginRegister(line);
                }

                // Sende alten Chat-Verlauf
                String history = loadChatHistory();
                sendMessage("=== Chat History ===\n" + history + "====================");

                // Nachrichten/Kommandos lesen
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        sendMessage("Goodbye!");
                        closeConnection();
                        return;

                    } else if (message.startsWith("/dm ")) {
                        // /dm Bob Hallo Bob!
                        String[] parts = message.split(" ", 3);
                        if (parts.length < 3) {
                            sendMessage("Usage: /dm <username> <message>");
                        } else {
                            String target = parts[1];
                            String dmMsg = parts[2];
                            sendDirectMessage(username, target, dmMsg);
                        }

                    } else {
                        // Öffentliche Nachricht an alle
                        broadcast(username + ": " + message);
                    }
                }

            } catch (IOException e) {
                closeConnection();
            }
        }

        private void handleLoginRegister(String input) {
            if (input.startsWith("/login ")) {
                String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    sendMessage("Usage: /login <user> <pass>");
                    return;
                }
                String user = parts[1];
                String pass = parts[2];
                if (!userManager.userExists(user)) {
                    sendMessage("User does not exist. Try /register <user> <pass>.");
                } else {
                    if (userManager.checkPassword(user, pass)) {
                        this.username = user;
                        this.loggedIn = true;
                        // In userMap eintragen, damit er DMs empfangen kann
                        userMap.put(username, this);
                        sendMessage("Login successful. Welcome, " + username + "!");
                    } else {
                        sendMessage("Wrong password. Try again.");
                    }
                }
            } else if (input.startsWith("/register ")) {
                String[] parts = input.split(" ", 3);
                if (parts.length < 3) {
                    sendMessage("Usage: /register <user> <pass>");
                    return;
                }
                String user = parts[1];
                String pass = parts[2];
                if (userManager.userExists(user)) {
                    sendMessage("User already exists. Try /login <user> <pass>.");
                } else {
                    boolean success = userManager.registerUser(user, pass);
                    if (success) {
                        sendMessage("Registration successful! You can now /login " + user + " <pass>.");
                    } else {
                        sendMessage("Registration failed. Please try again.");
                    }
                }
            } else {
                sendMessage("Please /login <user> <pass> or /register <user> <pass> first.");
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void closeConnection() {
            try {
                // Aus der userMap entfernen
                if (username != null) {
                    userMap.remove(username);
                }
                connections.remove(this);

                if (in != null) in.close();
                if (out != null) out.close();
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(9696);
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}