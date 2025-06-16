package Networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Server class implements a basic multi-threaded server capable of handling multiple client
 * connections through socket communication. It manages client connections and communication
 * protocols, allowing clients to send and receive messages via separate threads.
 *
 */
public class Server implements Runnable {

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private static String password;

    /**
     * Executes the main server logic in a separate thread.
     * This method is responsible for initializing the server socket, accepting incoming client connections,
     * and managing the lifecycle of connected clients. Clients are handled by spawning new threads for each connection.
     *
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Server started");
            System.out.println("Listening for clients on port " + serverSocket.getLocalPort());

            running.set(true);

            while (running.get()) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                System.out.println("New client connected");
                System.out.println("Client Connected");
                System.out.println("Client count: " + clients.size());

                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Error while server was running");
        } finally {
            System.out.println("Server shutting down");
            terminate();
        }
    }

    /**
     * Broadcasts a message to all connected clients, ensuring synchronized access
     * to the collection of clients. The sender of the message is not excluded
     * and will also receive the broadcasted message.
     *
     * @param message the message to be broadcasted to all connected clients
     * @param sender  the client sending the message, typically the origin
     *                of the broadcast
     */
    public synchronized void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting message: " + message+ " inside the Server class");
        for (ClientHandler client : clients) {
                client.sendMessage(message);

        }
    }

    /**
     * Removes a specified client from the server's list of connected clients.
     * This method ensures synchronized access to the client list to avoid
     * concurrent modification issues. It logs the disconnection event and the
     * updated count of currently connected clients.
     *
     * @param clientHandler the client handler instance representing the client
     *                      to be removed from the server's list of connected clients
     */
    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcast("Client " + clientHandler.nickname + " disconnected", clientHandler);
        System.out.println("Client " + clientHandler.nickname + " disconnected");
        System.out.println("Client disconnected");
        System.out.println("Client count: " + clients.size());
    }

    public int getClientCount(){
        return clients.size();
    }

    /**
     * Terminates the server by closing the server socket and notifying all connected clients of the shutdown.
     * This method ensures that any active server-side resources, such as sockets or client connections,
     * are properly released to avoid resource leaks.
     *
     */
    private void terminate() {
        try {
            if (serverSocket != null) serverSocket.close();
            for (ClientHandler client : clients) {
                client.sendMessage("Server is shutting down");
            }
        } catch (IOException e) {
            // irrelevant here
        }
    }

    /**
     * Validates whether the provided password attempt matches the server's stored password.
     *
     * @param passwordAttempt the password string provided for validation
     * @return true if the provided password matches the stored password, false otherwise
     */
    public boolean passwordValid(String passwordAttempt){
        return passwordAttempt.equals(password);
    }

    /**
     * The main entry point of the application. This method prompts the user to enter a password,
     * initializes the server, and starts it on a new thread.
     *
     * @param args command-line arguments passed to the program
     */
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter a password to start the server: ");
        password = s.nextLine();
        System.out.println("Password entered: " + password);

        Server server = new Server();
        new Thread(server).start();
    }
}

