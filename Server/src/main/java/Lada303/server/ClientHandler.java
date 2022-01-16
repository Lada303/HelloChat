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
    private String name;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = String.valueOf(socket.getPort());
            out.writeUTF("Hello from Server!");
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

    public String getName() {
        return name;
    }

    private void readMessage() throws IOException {
        String inStr;
        while (true) {
            inStr = in.readUTF();
            if (inStr.equalsIgnoreCase("/end")) {
                System.out.println("Client disconnected - " + socket.getRemoteSocketAddress());
                sendMessage("Server: you disconnected!");
                break;
            }
            server.broadcastMsg(inStr, name);
        }
    }

    protected void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
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
