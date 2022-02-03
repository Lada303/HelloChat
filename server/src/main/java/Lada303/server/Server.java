package Lada303.server;

import Lada303.service.ServiceCommands;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8189;
    private volatile boolean isStopServer;
    private List<ClientHandler> clients;
    private AuthService authService;
    private DbServerWork db;

    public Server() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            isStopServer = false;
            //поток - слушатель сканера для ввода команд администратора сервера
            Thread listenerSc = new Thread(this::listenerScanner);
            listenerSc.setDaemon(true);
            listenerSc.start();

            clients = new CopyOnWriteArrayList<>();
            db = DbServerWork.getDb();
            authService = JdbcAuthService.getJdbcAuthService(db);
            while (!isStopServer) {
                System.out.println("Server run, waiting connection");
                Socket socket = serverSocket.accept();
                if (!isStopServer) {
                    System.out.println("Client connected" + socket.getRemoteSocketAddress());
                    new ClientHandler(this, socket);
                }
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

    private void listenerScanner()  {
        Scanner sc = new Scanner(System.in);
        String command;
        while (sc.hasNext()) {
            command = sc.nextLine();
            if (command.equals("/stop")) {
                isStopServer = true;
                try {
                    new Socket(SERVER_ADDRESS, SERVER_PORT).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (ClientHandler client : clients) {
                    client.sendMessage("Server stopped!!!");
                    client.sendMessage(ServiceCommands.END);
                }
                return;
            }
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
