package lk.java.auctionsystem;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.io.*;
import java.net.Socket;


public class ClientController {


    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox       chatBox;
    @FXML private TextField  txtMessage;
    @FXML private VBox       userListBox;
    @FXML private Label      onlineCountLabel;
    @FXML private Label      userNameLabel;




    private ObjectOutputStream outputStream;
    private ObjectInputStream  inputStream;
    private Socket             socket;


    private String userName;


    public void initializeWithName(String name) {
        this.userName = name;
        userNameLabel.setText("👤  " + name);
        connectToServer();
    }


    private void connectToServer() {
        Thread t = new Thread(() -> {
            try {
                socket       = new Socket("127.0.0.1", 6000);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush();
                inputStream  = new ObjectInputStream(socket.getInputStream());


                outputStream.writeObject(
                        new Message(Message.Type.SYSTEM, userName, "joined"));
                outputStream.flush();


                while (true) {
                    Message msg = (Message) inputStream.readObject();
                    Platform.runLater(() -> handleIncoming(msg));
                }
            } catch (IOException e) {
                Platform.runLater(() ->
                        displaySystemMsg("  Connection lost: " + e.getMessage()));
            } catch (ClassNotFoundException e) {
                Platform.runLater(() ->
                        displaySystemMsg("  Protocol error: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void dispatch(Message msg) {
        Thread t = new Thread(() -> {
            try {
                outputStream.writeObject(msg);
                outputStream.flush();
                outputStream.reset();
            } catch (IOException e) {
                Platform.runLater(() ->
                        displaySystemMsg("  Send failed: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }


    private void handleIncoming(Message msg) {
        switch (msg.getType()) {
            case TEXT            -> addTextBubble(msg);
            case SYSTEM          -> displaySystemMsg(msg.getContent());
            case USER_LIST       -> updateUserList(msg.getContent());
        }

        chatScrollPane.setVvalue(1.0);
    }


    private void addTextBubble(Message msg) {
        boolean mine = msg.getSenderName().equals(userName);

        HBox row = new HBox();
        row.setPadding(new Insets(3, 16, 3, 16));
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(10, 16, 8, 16));

        if (!mine) {
            Label sender = new Label(msg.getSenderName());
            sender.setStyle("-fx-text-fill:#7289da;-fx-font-size:11.5px;-fx-font-weight:bold;");
            bubble.getChildren().add(sender);
        }

        Label text = new Label(msg.getContent());
        text.setWrapText(true);
        text.setStyle("-fx-font-size:14px;-fx-text-fill:" +
                (mine ? "#ffffff" : "#dce3f5") + ";");

        Label time = new Label(msg.getTimestamp());
        time.setStyle("-fx-font-size:10px;-fx-text-fill:" +
                (mine ? "rgba(255,255,255,0.55)" : "#6b7280") + ";");
        time.setMaxWidth(Double.MAX_VALUE);
        time.setStyle(time.getStyle() + (mine ? "-fx-alignment:center-right;" : ""));

        bubble.getChildren().addAll(text, time);
        bubble.setStyle(
            "-fx-background-color:" + (mine ? "#5865f2" : "#252d52") + ";" +
            "-fx-background-radius:" + (mine ? "18 4 18 18" : "4 18 18 18") + ";" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.28),6,0,0,2);"
        );

        row.getChildren().add(bubble);
        chatBox.getChildren().add(row);
    }



    private void displaySystemMsg(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(9, 16, 9, 16));

        Label lbl = new Label(text);
        lbl.setStyle(
            "-fx-background-color:rgba(88,101,242,0.13);" +
            "-fx-text-fill:#8892b0;" +
            "-fx-font-size:12px;" +
            "-fx-background-radius:20;" +
            "-fx-padding:4 18 4 18;"
        );
        row.getChildren().add(lbl);
        chatBox.getChildren().add(row);
    }

    private void updateUserList(String csv) {
        userListBox.getChildren().clear();
        if (csv == null || csv.isBlank()) {
            onlineCountLabel.setText("0");
            return;
        }

        String[] users = csv.split(",");
        int count = 0;
        for (String u : users) if (!u.isBlank()) count++;
        onlineCountLabel.setText(String.valueOf(count));

        for (String user : users) {
            String name = user.trim();
            if (name.isEmpty()) continue;

            boolean isMe = name.equals(userName);

            HBox userRow = new HBox(10);
            userRow.setAlignment(Pos.CENTER_LEFT);
            userRow.setPadding(new Insets(7, 14, 7, 14));

            Circle dot = new Circle(5, Color.web(isMe ? "#a8b4ff" : "#00d26a"));

            Label nameLbl = new Label(name + (isMe ? "  (you)" : ""));
            nameLbl.setStyle("-fx-text-fill:" + (isMe ? "#a8b4ff" : "#b0bec5") +
                    ";-fx-font-size:13px;" + (isMe ? "-fx-font-weight:bold;" : ""));

            userRow.getChildren().addAll(dot, nameLbl);
            if (isMe) {
                userRow.setStyle("-fx-background-color:rgba(88,101,242,0.12);" +
                        "-fx-background-radius:8;");
            }
            userListBox.getChildren().add(userRow);
        }
    }

    @FXML
    private void sendOnAction(ActionEvent event) {
        String text = txtMessage.getText().trim();
        if (text.isEmpty() || socket == null || socket.isClosed()) return;
        dispatch(new Message(Message.Type.TEXT, userName, text));
        txtMessage.clear();
    }


}
