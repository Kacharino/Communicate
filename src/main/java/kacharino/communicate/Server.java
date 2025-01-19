package kacharino.communicate;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Diese Klasse repräsentiert einen einfachen Chat-Server, der mehrere Clients
 * simultan bedienen kann. Sie bietet öffentliche Nachrichten (Broadcast) sowie
 * Direktnachrichten zwischen einzelnen Benutzern.
 * <p>
 * Der Server speichert alle gesendeten Nachrichten in einer Datei (chat_history.txt).
 * Zusätzlich verwaltet er mithilfe einer UserManager-Instanz die Registrierung
 * und das Login von Benutzern (users.txt).
 */
public class Server implements Runnable {

    private final ServerSocket server;
    private boolean isRunning;

    private final List<ConnectionHandler> connections;
    private final Map<String, ConnectionHandler> userMap;   // username -> ConnectionHandler

    private final File chatHistoryFile;
    private final UserManager userManager;

    /**
     * Erstellt einen neuen Server-Socket auf dem angegebenen Port und bereitet
     * die nötigen Datenstrukturen für den Chatbetrieb vor.
     *
     * @param port der Port, auf dem der Server lauschen soll
     * @throws IOException falls der ServerSocket nicht geöffnet werden kann
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.isRunning = true;
        this.connections = new CopyOnWriteArrayList<>();
        this.userMap = new ConcurrentHashMap<>();
        this.chatHistoryFile = new File("chat_history.txt");
        this.userManager = new UserManager("users.txt");
    }

    /**
     * Startet die Hauptschleife des Servers. Nimmt neue Client-Verbindungen an und
     * erzeugt für jede Verbindung einen eigenen Thread (ConnectionHandler).
     * <p>
     * Sollte isRunning false sein, wird die Schleife beendet und keine neuen
     * Verbindungen mehr angenommen.
     */
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
     * Sendet eine öffentliche Nachricht (Broadcast) an alle verbundenen Clients
     * und speichert diese zusätzlich in der Chat-Historie.
     *
     * @param message die zu broadcastende Nachricht
     */
    public synchronized void broadcast(String message) {
        saveMessageToFile(message);
        for (ConnectionHandler ch : connections) {
            ch.sendMessage(message);
        }
    }

    /**
     * Sendet eine Direktnachricht von einem Absender zu einem Empfänger (falls
     * dieser online ist). Die Nachricht wird ebenfalls in der Chat-Historie
     * abgelegt.
     *
     * @param fromUser Name des sendenden Benutzers
     * @param toUser   Name des Ziel-Benutzers
     * @param msg      Inhalt der Nachricht
     */
    public synchronized void sendDirectMessage(String fromUser, String toUser, String msg) {
        ConnectionHandler targetHandler = userMap.get(toUser);
        ConnectionHandler fromHandler = userMap.get(fromUser);

        String directMsg = "[DM] " + fromUser + " -> " + toUser + ": " + msg;
        saveMessageToFile(directMsg);

        if (targetHandler != null) {
            targetHandler.sendMessage(directMsg);
        } else {
            if (fromHandler != null) {
                fromHandler.sendMessage("User '" + toUser + "' not found or not logged in.");
            }
        }
        // Sender sieht die eigene Nachricht ebenfalls als Bestätigung
        if (fromHandler != null) {
            fromHandler.sendMessage(directMsg);
        }
    }

    /**
     * Schreibt eine gegebene Nachricht in die Datei chat_history.txt.
     *
     * @param message die zu speichernde Nachricht
     */
    private void saveMessageToFile(String message) {
        try (FileWriter fw = new FileWriter(chatHistoryFile, true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    /**
     * Lädt alle bisher gespeicherten Nachrichten aus chat_history.txt.
     *
     * @return kompletter Chatverlauf als String oder ein leerer String,
     *         falls keine Datei vorhanden bzw. noch keine Nachrichten existieren
     */
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

    /**
     * Beendet den Server-Betrieb und schließt alle offenen Verbindungen.
     * Anschließend werden keine neuen Clients mehr angenommen.
     */
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

    /**
     * Dieser ConnectionHandler kümmert sich um die Kommunikation mit einem einzelnen
     * Client. Pro verbundenem Client wird eine eigene Instanz erzeugt.
     */
    private class ConnectionHandler implements Runnable {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean loggedIn;
        private String username;

        /**
         * Erzeugt einen neuen ConnectionHandler für den gegebenen Socket.
         *
         * @param socket die Socket-Verbindung zum Client
         */
        public ConnectionHandler(Socket socket) {
            this.socket = socket;
            this.loggedIn = false;
        }

        /**
         * Hauptablauf für einen einzelnen Client:
         * <ul>
         *   <li>Login-/Registrier-Schleife</li>
         *   <li>Senden des vorhandenen Chatverlaufs</li>
         *   <li>Empfangen und Verarbeiten von Chat-Befehlen / Nachrichten</li>
         * </ul>
         */
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

                // Chat-Verlauf an neu eingeloggten Nutzer senden
                String history = loadChatHistory();
                sendMessage("=== Chat History ===\n" + history + "====================");

                // Eingehende Nachrichten/Befehle verarbeiten
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
                        // Öffentliche Nachricht
                        broadcast(username + ": " + message);
                    }
                }

            } catch (IOException e) {
                closeConnection();
            }
        }

        /**
         * Verarbeitet Login- und Registrierungsbefehle des Clients.
         * Ist der Nutzer erfolgreich eingeloggt, wird loggedIn auf true gesetzt.
         *
         * @param input Textzeile, die der Client geschickt hat (z.B. "/login Alice 1234")
         */
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
                        // Ermögliche Direktnachrichten (Key in userMap)
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

        /**
         * Sendet eine Nachricht direkt an diesen Client.
         *
         * @param msg Text, der an den Client gesendet wird
         */
        public void sendMessage(String msg) {
            out.println(msg);
        }

        /**
         * Schließt die Verbindung zum Client und entfernt ihn aus
         * allen relevanten Datenstrukturen.
         */
        public void closeConnection() {
            try {
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

    /**
     * Einstiegspunkt des Programms: Erzeugt einen Server auf Port 9696 und
     * startet ihn in einem eigenen Thread.
     *
     * @param args nicht verwendet
     */
    public static void main(String[] args) {
        try {
            Server server = new Server(9696);
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
