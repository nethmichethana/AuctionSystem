package lk.java.auctionsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class ServerInitializer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                ServerInitializer.class.getResource("server-view.fxml"));
        Scene scene = new Scene(loader.load(), 720, 540);
        scene.getStylesheets().add(
                ServerInitializer.class.getResource("chat.css").toExternalForm());

        stage.setTitle("GroupChat  –  Server");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(450);
        stage.show();
    }
}
