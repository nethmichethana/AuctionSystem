package lk.java.auctionsystem;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientHandler implements Runnable {


    private static final List<ClientHandler> ALL_CLIENTS =
            Collections.synchronizedList(new ArrayList<>());

    private static final List<Message> MESSAGE_HISTORY =
            Collections.synchronizedList(new ArrayList<>());

    private static final int MAX_HISTORY = 200;

    private final Socket           socket;
    private final ServerController server;
    private ObjectOutputStream     out;
    private ObjectInputStream      in;
    private String                 clientName;


    public ClientHandler(Socket socket, ServerController server) throws IOException {
        this.socket = socket;
        this.server = server;

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {

            Message joinMsg = (Message) in.readObject();
            this.clientName = joinMsg.getSenderName();

            ALL_CLIENTS.add(this);
            server.updateLog( clientName + "  connected  from  " +
                    socket.getInetAddress().getHostAddress());
            server.updateClientCount(ALL_CLIENTS.size());


            synchronized (MESSAGE_HISTORY) {
                for (Message historical : MESSAGE_HISTORY) {
                    sendToThis(historical);
                }
            }


            Message joinAnnounce = new Message(
                    Message.Type.SYSTEM, "Server",
                    "🟢  " + clientName + " joined the ClientsChat");
            addToHistory(joinAnnounce);
            broadcastToAll(joinAnnounce);
            broadcastUserList();


            while (true) {
                Message msg = (Message) in.readObject();
                addToHistory(msg);
                broadcastToAll(msg);

                String logLine = (msg.getType() == Message.Type.TEXT)
                        ? msg.getContent()
                        : "[" + msg.getType() + "]  " + msg.getFileName();
                server.updateLog("[" + clientName + "]  " + logLine);
            }

        } catch (IOException | ClassNotFoundException e) {
        } finally {
            disconnect();
        }
    }


    private void disconnect() {
        ALL_CLIENTS.remove(this);
        if (clientName != null) {
            Message leaveMsg = new Message(
                    Message.Type.SYSTEM, "Server",
                    "🔴  " + clientName + " left the ClientsChat");
            addToHistory(leaveMsg);
            broadcastToAll(leaveMsg);
            broadcastUserList();
            server.updateLog( clientName + " disconnected");
            server.updateClientCount(ALL_CLIENTS.size());
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    private void broadcastToAll(Message msg) {
        List<ClientHandler> snapshot = new ArrayList<>(ALL_CLIENTS);
        for (ClientHandler handler : snapshot) {
            handler.sendToThis(msg);
        }
    }

    private void broadcastUserList() {
        StringBuilder csv = new StringBuilder();
        synchronized (ALL_CLIENTS) {
            for (ClientHandler h : ALL_CLIENTS) {
                if (h.clientName != null) {
                    csv.append(h.clientName).append(",");
                }
            }
        }
        Message userListMsg = new Message(Message.Type.USER_LIST, "Server", csv.toString());
        List<ClientHandler> snapshot = new ArrayList<>(ALL_CLIENTS);
        for (ClientHandler handler : snapshot) {
            handler.sendToThis(userListMsg);
        }
    }

    public void sendToThis(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException ignored) {
        }
    }

    private static void addToHistory(Message msg) {
        synchronized (MESSAGE_HISTORY) {
            MESSAGE_HISTORY.add(msg);
            if (MESSAGE_HISTORY.size() > MAX_HISTORY) {
                MESSAGE_HISTORY.remove(0);
            }
        }
    }


    public static void clearHistory() {
        MESSAGE_HISTORY.clear();
        ALL_CLIENTS.clear();
    }
}
