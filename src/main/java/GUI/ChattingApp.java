package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main entry point for the JavaFX-based chat application. Sets up the primary stage with the Login interface.
 * Once launched, the user can log in and begin interacting with the chat system
 * The Server the user logs in is protexted via a Password and the user gets to choose their nickname
 */
public class ChattingApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChattingApp.class.
                getResource("/fxmlFiles/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/cssFiles/style.css").toExternalForm());
        stage.setTitle("Chatting Application");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}