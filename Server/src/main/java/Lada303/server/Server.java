package Lada303.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final int SERVER_PORT = 8189;
    private List<ClientHandler> clients;

    public Server() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            clients = new CopyOnWriteArrayList<>();
            while (true) {
                System.out.println("Server run, waiting connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected" + socket.getRemoteSocketAddress());
                clients.add(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Server error: " + e.getMessage());
        } finally {
            System.out.println("Server stopped");
        }
    }

    protected void broadcastMsg(String msg, String name) {
        for (ClientHandler client : clients) {
            client.sendMessage(name + ": " + msg);
        }
    }

}
