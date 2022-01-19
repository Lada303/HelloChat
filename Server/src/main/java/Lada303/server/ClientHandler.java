package Lada303.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    //private boolean isAuthenticated;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Server: Hello!");
            new Thread(() -> {
                try {
                    readMessage();
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println(e.getMessage());
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("ClientHandler error: " + e.getMessage());
        }
    }

    public String getNick() {
        return nick;
    }

    private void readMessage() throws IOException {
        String inStr;
        M: while (true) {
            inStr = in.readUTF();
            if (inStr.startsWith("/")) {
                String[] tokens = inStr.split(" ", 3);
                switch (tokens[0]) {
                    case "/auth":   nick = server.getAuthService().getNick(tokens[1], tokens[2]);
                                    if (nick != null) {
                                        System.out.println("Client authenticated: " + nick + socket.getRemoteSocketAddress());
                                        sendMessage("Server: you authenticated - " + nick);
                                        server.subscribe(this);
                                    } else {
                                        sendMessage("Server: wrong login or password.");
                                    }
                                    break;
                    case "/end":    System.out.println("Client disconnected: "+ nick + socket.getRemoteSocketAddress());
                                    sendMessage("Server: you disconnected!");
                                    server.unsubscribe(this);
                                    break M;
                    case "/w":      sendMessage(nick + " to " + tokens[1] + ": " + tokens[2]);
                                    server.privateMsg(nick, tokens[1], tokens[2]);
                                    break;
                    default: sendMessage("Server: Non-existent command");
                }
                continue;
            }
            server.broadcastMsg(nick, inStr);
        }
    }

    protected void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Exp: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
