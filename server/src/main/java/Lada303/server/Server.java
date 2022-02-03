package Lada303.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final int SERVER_PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;
    private DbServerWork db;

    public Server() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            clients = new CopyOnWriteArrayList<>();
            db = DbServerWork.getDb();
            authService = JdbcAuthService.getJdbcAuthService(db);
            while (true) {
                System.out.println("Server run, waiting connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected" + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
        } finally {
            if (db != null) {
                db.disconnect();
            }
            System.out.println("Server stopped");
        }
    }

    protected AuthService getAuthService() {
        return authService;
    }

    protected void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    protected void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    protected boolean isSubscribed(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    protected void broadcastClientList() {
        StringBuilder clientList = new StringBuilder("/clients");
        for (ClientHandler client : clients) {
            clientList.append(" ");
            clientList.append(client.getNick());
        }
        System.out.println(clientList);
        for (ClientHandler client : clients) {
            client.sendMessage(String.valueOf(clientList));
        }
    }

    protected void broadcastMsg(String msgSender, String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msgSender + ": " + msg);
        }
    }

    protected void privateMsg(String msgSender, String msgRecipient, String msg) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(msgRecipient)) {
                client.sendMessage(msgSender + " to " + msgRecipient+ ": " + msg);
                return;
            }
        }
        for (ClientHandler client : clients) {
            if (client.getNick().equals(msgSender)) {
                client.sendMessage("Server: msg not received, " + msgRecipient + " offline");
                break;
            }
        }

    }
}
