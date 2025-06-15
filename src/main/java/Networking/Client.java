package Networking;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client implements Runnable {

    Socket socket = null;
    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    public AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Callback mechanism that listens for incoming messages.
     */
    public interface MessageListener {
        void onMessageReceived(String message);
    }

    private MessageListener messageListener;

    /**
     * A callback interface for handling connection errors that occur during the operation of a client.
     */
    public interface ConnectionErrorListener {
        void onConnectionError(Exception e);
    }

    private ConnectionErrorListener errorListener;

    /**
     * Constructs a new Client instance with the provided message listener and error listener.
     */
    public Client(MessageListener messageListener, ConnectionErrorListener errorListener) {
        this.messageListener = messageListener;
        this.errorListener = errorListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Executes the main logic of the client in a separate thread.
     * Establishes a connection to a server, listens for incoming messages,
     * and invokes the appropriate callbacks for message reception and error handling.
     *
     */
    @Override
    public void run() {
        try{

            socket = new Socket("localhost", 1234);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            String message;
            while(running.get() && (message = bufferedReader.readLine()) != null) {
                if(messageListener != null)
                    messageListener.onMessageReceived(message);
            }
        }
        catch (Exception e){
            if (errorListener != null)
                errorListener.onConnectionError(e);
            e.printStackTrace();
            System.out.println("Error while client was running");
        }
        finally {
            System.out.println("Client terminated");
            terminate();
        }
    }

    /**
     * Sends a message to the connected server using the current buffered writer.
     * @param message the string message to be sent to the server
     */
    public void sendMessage(String message){
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while sending message");
            terminate();
        }
    }

    /**
     * Terminates the client's operation by closing all resources and stopping its execution.
     */
    private void terminate() {
        running.set(false);
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // irrelevant here
        }
    }
}
