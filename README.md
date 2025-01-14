# Nutzung

Sobald der Client mit dem Server verbunden ist, erhältst du folgende Aufforderung:

Welcome to the Chat Server!
Use: /login   or /register

Nachstehend findest du die **Befehle** und deren **Verwendung** (Usage):

1. **Registrierung**
    - **Befehl**: `/register <username> <password>`
    - **Beispiel**: `/register Alice 1234`
    - **Beschreibung**: Legt einen neuen Benutzer `Alice` mit Passwort `1234` an, sofern er noch nicht existiert.

2. **Login**
    - **Befehl**: `/login <username> <password>`
    - **Beispiel**: `/login Alice 1234`
    - **Beschreibung**: Meldet sich unter dem gewählten Benutzernamen `Alice` an. Erst nach erfolgreichem Login kann man Nachrichten verschicken.

3. **Öffentliche Nachricht**
    - **Befehl**: Einfach Text eingeben ohne Schrägstrich
    - **Beispiel**: `Hallo zusammen!`
    - **Beschreibung**: Sendet eine Nachricht an **alle** eingeloggten Nutzer.

4. **Direktnachricht**
    - **Befehl**: `/dm <username> <message>`
    - **Beispiel**: `/dm Bob Hi Bob!`
    - **Beschreibung**: Sendet eine private Nachricht an `Bob`. Nur `Bob` und der Sender sehen diese Nachricht.

5. **Verbindung beenden**
    - **Befehl**: `/quit`
    - **Beschreibung**: Trennt die Client-Verbindung zum Server.

> **Hinweis**: Bei jedem Login wird der gesamte, bislang aufgezeichnete Chatverlauf aus der Datei `chat_history.txt` geladen und dem Nutzer angezeigt.  