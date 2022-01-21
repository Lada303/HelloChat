package Lada303.server;

import Lada303.service.ServiceCommands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
                    socket.setSoTimeout(120000);
                    readMessage();
                } catch (SocketTimeoutException e) {
                    System.out.println("Inactive client, disconnected - " + socket.getRemoteSocketAddress());
                    sendMessage(ServiceCommands.END);
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
                    case ServiceCommands.AUTH:
                        nick = server.getAuthService().getNick(tokens[1], tokens[2]);
                        if (nick != null) {
                            if (server.isSubscribed(nick)) {
                                sendMessage("Server: you are already online in another window.");
                            } else {
                                System.out.println("Client authenticated: " + nick + socket.getRemoteSocketAddress());
                                sendMessage(ServiceCommands.AUTH_OK + " " + nick);
                                socket.setSoTimeout(0);
                                server.subscribe(this);
                            }
                        } else {
                            sendMessage("Server: wrong login or password.");
                        }
                        break;
                    case ServiceCommands.REG:
                        tokens = inStr.split(" ", 4);
                        if (tokens.length < 4 ||
                                !server.getAuthService().registration(tokens[1], tokens[2], tokens[3])) {
                            sendMessage(ServiceCommands.REG_NO);
                            break;
                        }
                        nick = tokens[3];
                        sendMessage(ServiceCommands.REG_OK + " " + nick);
                        socket.setSoTimeout(0);
                        server.subscribe(this);
                        break;
                    case ServiceCommands.END:
                        System.out.println("Client disconnected: "+ nick + socket.getRemoteSocketAddress());
                        sendMessage(ServiceCommands.END);
                        server.unsubscribe(this);
                        break M;
                    case ServiceCommands.W:
                        if (tokens.length < 3) {
                            sendMessage("Server: wrong massage (no recipient or message text).");
                            break;
                        }
                        if (tokens[1].equals(nick)) {
                            sendMessage("Server: you try send massage yourself.");
                            break;
                        }
                        sendMessage(String.format("%s to %s: %s", nick, tokens[1], tokens[2]));
                        server.privateMsg(nick, tokens[1], tokens[2]);
                        break;
                    default:
                        sendMessage("Server: Non-existent command");
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
