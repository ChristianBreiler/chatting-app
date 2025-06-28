package GUI;

import Networking.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button connectButton;

    @FXML
    private TextArea errorField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField usernameField;

    Client client;
    String username;
    String password;

    /**
     * Initializes the user interface settings and behavior for the login view.
     */
    @FXML
    void initialize() {
        errorField.setEditable(false);
        passwordField.setOnAction(e -> connectButton.fire());
        usernameField.setOnAction(e -> connectButton.fire());
    }

    /**
     * Handles the connection process when the "Connect" button is clicked.
     * Validates the user input, initializes the client connection,
     * and sends login credentials to the server.
     * Updates the user interface in case of errors or login status changes.
     *
     * @param event the action event triggered by clicking the "Connect" button
     */
    @FXML
    void connect(ActionEvent event) {
        username = usernameField.getText().trim();
        password = passwordField.getText().trim();

        if (username.isEmpty() || username.length() > 15 || username.contains(":")) {
            errorField.setText("Invalid username");
            return;
        }

        if (password.isEmpty()) {
            errorField.setText("Please enter a password");
            return;
        }

        client = new Client(
                message -> Platform.runLater(() -> {
                    switch (message) {
                        case "SERVER_FULL":
                            errorField.setText("Server is full");
                            break;
                        case "LOGIN_SUCCESS":
                            openChatWindow();
                            break;
                        case "LOGIN_FAILED":
                            errorField.setText("Invalid credentials");
                            break;
                    }
                }),
                error -> Platform.runLater(() -> {
                    errorField.setText("Connection failed");
                })
        );

        Thread clientThread = new Thread(client);
        clientThread.setDaemon(true);
        clientThread.start();

        new Thread(() -> {
            try {
                Thread.sleep(200);
                client.sendMessage("LOGIN:" + username + ":" + password);
            } catch (Exception e) {
                Platform.runLater(() -> errorField.setText("Failed to send login"));
            }
        }).start();
    }

    /**
     * Opens the chat window for the user by loading the Chatting.fxml interface and initializing
     * necessary settings, such as attaching the client and username to the ChattingAppController instance.
     * This method also applies a custom stylesheet to the scene and transitions from the current login window
     * to the chat window.
     */
    private void openChatWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/Chatting.fxml"));
            Parent root = loader.load();

            ChattingAppController controller = loader.getController();
            controller.setClient(client);
            controller.setNickname(username);

            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/cssFiles/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Chat Room");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            Stage currentStage = (Stage) connectButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            errorField.setText("Error loading chat window");
            e.printStackTrace();
        }
    }
}
