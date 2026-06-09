package lk.java.auctionsystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;


public class ClientInitializer extends Application {

    @Override
    public void start(Stage stage) throws IOException {


        TextInputDialog dialog = new TextInputDialog("User" + (int)(Math.random() * 9000 + 1000));
        dialog.setTitle("ClientsChat  –  Join");
        dialog.setHeaderText("👋  Welcome to ClientsCht!");
        dialog.setContentText("User Name:");
        dialog.initStyle(StageStyle.UTILITY);


        String css = ClientInitializer.class.getResource("chat.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().add(css);
        dialog.getDialogPane().setStyle("-fx-background-color:#9fa3bc;");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty() || result.get().trim().isEmpty()) {
            Platform.exit();
            return;
        }

        String userName = result.get().trim();


        FXMLLoader loader = new FXMLLoader(
                ClientInitializer.class.getResource("client-view.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(css);

        ClientController controller = loader.getController();
        controller.initializeWithName(userName);

        stage.setTitle("ClientsChat  –  " + userName);
        stage.setScene(scene);
        stage.setMinWidth(720);
        stage.setMinHeight(520);
        stage.show();
    }
}
