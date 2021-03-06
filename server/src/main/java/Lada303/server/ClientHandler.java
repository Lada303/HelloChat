package Lada303.server;

import Lada303.service.ServiceCommands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class ClientHandler {
    private  static  final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    protected ClientHandler(Server server, Socket socket) {
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
                    LOGGER.info("Inactive client, disconnected - " + socket.getRemoteSocketAddress());
                    sendMessage(ServiceCommands.END);
                } catch (IOException e) {
                    //e.printStackTrace();
                    LOGGER.warning(e.getMessage());
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.warning("ClientHandler error: " + e.getMessage());
        }
    }

    protected String getNick() {
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
                        authMethod(tokens);
                        continue;
                    case ServiceCommands.REG:
                        regMethod(inStr);
                        continue;
                    case ServiceCommands.W:
                        wMethod(tokens);
                        continue;
                    case ServiceCommands.CHG:
                        chgMethod(inStr);
                        continue;
                    case ServiceCommands.END:
                        endMethod();
                        break M;
                    default:
                        sendMessage("Server: Non-existent command");
                }
            }
            server.broadcastMsg(nick, inStr);
        }
    }

    protected void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            //e.printStackTrace();
            LOGGER.warning("Exp: " + e.getMessage());
        }
    }

    private void authMethod(String[] tokens) throws SocketException {
        nick = server.getAuthService().getNick(tokens[1], tokens[2]);
        if (nick != null) {
            if (server.isSubscribed(nick)) {
                sendMessage("Server: you are already online in another window.");
            } else {
                LOGGER.info("Client authenticated: " + nick + socket.getRemoteSocketAddress());
                sendMessage(ServiceCommands.AUTH_OK + " " + nick + " " + tokens[1]);
                socket.setSoTimeout(0);
                server.subscribe(this);
            }
        } else {
            sendMessage("Server: wrong login or password.");
            LOGGER.info("Client not authenticated: " + socket.getRemoteSocketAddress());
        }
    }

    private void regMethod(String inStr) throws SocketException {
        String[] tokens = inStr.split(" ", 4);
        if (tokens.length < 4 ||
                !server.getAuthService().registration(tokens[1], tokens[2], tokens[3])) {
            sendMessage(ServiceCommands.REG_NO);
            LOGGER.info("Client not reg: " + socket.getRemoteSocketAddress());
            return;
        }
        nick = tokens[3];
        sendMessage(ServiceCommands.REG_OK + " " + nick + " " + tokens[1]);
        LOGGER.info("Client is reg: " + nick + socket.getRemoteSocketAddress());
        socket.setSoTimeout(0);
        server.subscribe(this);
    }

    private void wMethod(String[] tokens) {
        if (tokens.length < 3) {
            sendMessage("Server: wrong massage (no recipient or message text).");
            return;
        }
        if (tokens[1].equals(nick)) {
            sendMessage("Server: you try send message yourself.");
            return;
        }
        if (!server.privateMsg(nick, tokens[1], tokens[2])) {
            sendMessage("Server: msg not received, " + tokens[1] + " offline");
        } else {
            sendMessage(String.format("%s to %s: %s", nick, tokens[1], tokens[2]));
        }
    }

    private void chgMethod(String inStr) {
        String[] tokens = inStr.split(" ", 4);
        if (tokens.length < 4 ||
                !server.getAuthService().changeNick(tokens[1], tokens[2], tokens[3])) {
            sendMessage(ServiceCommands.CHG_NO);
            LOGGER.info("Client nick not changed " + nick + socket.getRemoteSocketAddress());
            return;
        }
        nick = tokens[3];
        sendMessage(ServiceCommands.CHG_OK + " " + nick);
        LOGGER.info("Client changed nick: " + nick + socket.getRemoteSocketAddress());
        server.broadcastClientList();
    }

    private void endMethod() {
        LOGGER.info("Client disconnected: "+ nick + socket.getRemoteSocketAddress());
        sendMessage(ServiceCommands.END);
        server.unsubscribe(this);
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
            //e.printStackTrace();
            LOGGER.warning("Exc: " + e.getMessage());
        }
    }
}
