package lk.java.auctionsystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerController {

    @FXML private TextArea logArea;
    @FXML private Label    statusLabel;
    @FXML private Label    clientCountLabel;

    private static final int PORT       = 6000;
    private static final DateTimeFormatter LOG_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private ServerSocket serverSocket;

    public void initialize() {
        ClientHandler.clearHistory();
        startServer();
    }


    private void startServer() {
        Thread t = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                Platform.runLater(() -> {
                    appendLog(" Auction Server started - port " + PORT);
                    appendLog(" Waiting for clients to connect…");
                    appendLog("Item | Vintage watch | Starting price : LKR 5000");
                    statusLabel.setText(" RUNNING  on port " + PORT);
                });


                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    try {
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        Thread worker = new Thread(handler);
                        worker.setDaemon(true);
                        worker.start();
                    } catch (IOException e) {
                        appendLog("  Failed to create handler: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() ->
                        appendLog("  Server error: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }



    public void updateLog(String message) {
        Platform.runLater(() -> appendLog(message));
    }

    public void updateClientCount(int count) {
        Platform.runLater(() ->
                clientCountLabel.setText("Connected Clients:  " + count));
    }



    private void appendLog(String message) {
        String time = LocalDateTime.now().format(LOG_FMT);
        logArea.appendText("[" + time + "]  " + message + "\n");
    }
}