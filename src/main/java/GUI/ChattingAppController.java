package GUI;

import Networking.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ChattingAppController{

    @FXML
    private Label infoLabel;

    @FXML
    private TextField messageInput;

    @FXML
    private TextArea messageWindow;

    @FXML
    private Button sendButton;

    @FXML
    private MenuBar leaveMenu;

    private Client client;
    String nickname = "";

    /**
     * Initializes the chat UI settings and interaction behaviors for the chat application.
     */
    @FXML
    public void initialize() {
        messageWindow.setEditable(false);
        messageInput.setOnAction(e -> sendButton.fire());
    }

    /**
     * Handles the sending of a user message to the server. This method retrieves the message
     * typed in the input field, appends the users nickname, and sends it through the client connection.
     * If the input field is empty or consists only of whitespace, the method does nothing.
     *
     * @param event
     */
    @FXML
    void send(ActionEvent event) {

        String message = messageInput.getText();
        if(message.trim().equals(""))
            return;
        System.out.println("Sending message: " + message);
        client.sendMessage(nickname + ": " + message);
        messageInput.clear();
    }

    /**
     * Sets the {@link Client} instance for the chat application. This method also configures
     * the clients message listener to handle incoming messages. Messages received
     * from the client are appended to the message window on the JavaFX application thread.
     *
     * This method is used to transfer the client instance from the login screen to this controller
     *
     * @param client the client instance to be used for managing server communication
     */
    public void setClient(Client client) {
        this.client = client;

        client.setMessageListener(message -> Platform.runLater(()
                -> messageWindow.appendText(message + "\n")));
    }

    /**
     * Sets the user's nickname for the chat application and updates the display label
     * @param nickname the nickname to be set for the user
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
        Platform.runLater(() -> infoLabel.setText("Connected as " + nickname + " on Port 1234"));
    }


    @FXML
    void close(ActionEvent event) {
        System.out.println("Closing client");

        // Terminate the client thread and close the socket connection
        client.running.set(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.initialize();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/cssFiles/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Chatting Application");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            ((Stage) leaveMenu.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while loading login menu");
        }
    }



}