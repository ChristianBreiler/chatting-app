package Networking;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ClientHandler handles the interaction with a single connected client in a server-client architecture.
 * It is responsible for managing the communication between the server and the client,
 * including receiving and sending messages as well as validating login attempts.
 */
public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Server server;
    public String nickname;

    /**
     * Constructs a new ClientHandler instance, initializes the socket and corresponding
     * I/O streams used for communication, and assigns the associated server for
     * managing client-server interactions.
     *
     * @param socket the client's socket connection used for communication
     * @param server the server instance managing this client connection
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            terminate();
        }
    }

    /**
     * Terminates the client's connection and releases all associated resources.
     */
    private void terminate() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // List for longer messages to be cut into pieces
    List<String> messages = new ArrayList<>();

    /**
     * Sends a message to the connected client. If the message exceeds 90 characters
     * in length, it is broken into smaller segments to maintain readability.
     * After processing, all messages are written to the output stream
     *
     * @param message the message to be sent to the client; if longer than 90 characters,
     *                it will be split into multiple parts
     */
    void sendMessage(String message){
        if(message.length() > 90){
            cutUpMessage(message);
        }else
            messages.add(message);
        try {
            for(String msg : messages){
                writer.write(msg);
                writer.newLine();
            }
            writer.newLine();
            writer.flush();
            messages.clear();
        } catch (IOException e) {
           System.out.println("Error while sending message");
        }
    }

    /**
     * Splits a given message into smaller segments to ensure it adheres to a specific line length
     * limit while maintaining proper formatting. The initial segment includes the required namespace
     * prefix, and subsequent segments are aligned with preceding lines by adding appropriate spacing.
     *
     * @param message the string message to be split into smaller segments for proper formatting
     */
    private void cutUpMessage(String message) {
        boolean first = true;
        int nicknamespace = message.indexOf(":") + 4;
        String spaces = String.valueOf(' ').repeat(nicknamespace+1);
        int limit = 90;
        for(int i = 0; i < message.length(); i += limit){
            if(first){
                messages.add(message.substring(i, Math.min(message.length(), i + 90)));
                first = false;
                limit = 90 - nicknamespace - 1;
            }else
                messages.add(spaces + message.substring(i, Math.min(message.length(), i + limit)));
        }
        System.out.println("Message cut up into " + messages.size() + " pieces");
    }

    /**
     * Executes the main logic for handling client communication in a separate thread.
     * This method manages the lifecycle of a client connection, including:
     * 1. Validating the client's login credentials.
     * 2. Sending appropriate responses (`LOGIN_SUCCESS` or `LOGIN_FAILED`) based on the validation result.
     * 3. Continuously reading messages from the client, broadcasting them to other connected clients,
     *    and processing any exceptions or disconnections.
     * 4. Ensuring proper cleanup of resources and removal of the client from the server in case of disconnection or error.
     *
     */
    @Override
    public void run() {
        try {

            String loginAttempt = reader.readLine();
            if (!validatePassword(loginAttempt)) {
                sendMessage("LOGIN_FAILED");
                terminate();
                return;
            } else {
                sendMessage("LOGIN_SUCCESS");
            }

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received " + message);
                server.broadcast(message, this);
            }

        } catch (IOException e) {
            if (e instanceof java.net.SocketException && e.getMessage().contains("Connection reset")) {
                System.out.println("Client disconnected unexpectedly.");
            } else {
                System.out.println("An error occurred in ClientHandler");
            }
        } finally {
            server.removeClient(this);
            terminate();
        }
    }

    /**
     * Validates whether a given message contains the correct login format and password.
     * The message must have a specific structure starting with "LOGIN:", followed by
     * additional components separated by colons, and ending with the password.
     *
     * @param message the message string to be validated; it is expected to follow the format "LOGIN:<username>:<password>"
     * @return true if the message adheres to the expected format and the password is valid; false otherwise
     */
    private boolean validatePassword(String message) {
        if (message == null || !message.startsWith("LOGIN:"))
            return false;
        String[] parts = message.split(":");
        if (parts.length != 3)
            return false;

        String username = parts[1];
        String password = parts[2];

        boolean valid = server.passwordValid(password);
        if(valid){
            nickname = username;
            return true;
        }else return false;
    }


}
