package kacharino.communicate;

import java.io.*;

public class UserManager {
    private final File userFile;

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