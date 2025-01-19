package kacharino.communicate;

import java.io.*;

/**
 * Die Klasse UserManager verwaltet die Registrierung und Authentifizierung von Benutzern.
 * Benutzernamen und Passwörter werden in einer Textdatei gespeichert, die beim Instanziieren
 * dieser Klasse angegeben wird (z. B. "users.txt").
 */
public class UserManager {

    /**
     * Datei, in der Benutzername und Passwort gespeichert werden.
     */
    private final File userFile;

    /**
     * Erzeugt einen UserManager, der die Datei <code>filename</code> zur Speicherung
     * von Benutzerdaten verwendet. Falls die Datei nicht existiert, wird sie angelegt.
     *
     * @param filename Pfad zur Datei, die Benutzerinformationen enthält oder enthalten soll
     */
    public UserManager(String filename) {
        this.userFile = new File(filename);
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create user file: " + e.getMessage());
            }
        }
    }

    /**
     * Prüft, ob ein Benutzer in der Datei bereits existiert.
     *
     * @param username der Benutzername, nach dem gesucht wird
     * @return <code>true</code>, wenn der Benutzer vorhanden ist, sonst <code>false</code>
     */
    public boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equalsIgnoreCase(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registriert einen neuen Benutzer, sofern der Benutzername noch nicht vergeben ist.
     * Die Daten werden in <code>userFile</code> im Format "username:password" gespeichert.
     *
     * @param username Benutzername, der angelegt werden soll
     * @param password zugehöriges Passwort
     * @return <code>true</code>, wenn die Registrierung erfolgreich war, sonst <code>false</code>
     */
    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }
        try (FileWriter writer = new FileWriter(userFile, true)) {
            writer.write(username + ":" + password + "\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to user file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Überprüft, ob das angegebene Passwort zum Benutzernamen passt.
     *
     * @param username Benutzername, dessen Passwort verifiziert werden soll
     * @param password das zu prüfende Passwort
     * @return <code>true</code>, wenn Benutzername existiert und das Passwort korrekt ist,
     *         sonst <code>false</code>
     */
    public boolean checkPassword(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equalsIgnoreCase(username)) {
                    return parts[1].equals(password);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
        }
        return false;
    }
}
