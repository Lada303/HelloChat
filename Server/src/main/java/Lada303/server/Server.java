package Lada303.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    private final int SERVER_PORT = 8189;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public void serverRun() {
        //AtomicBoolean connection = new AtomicBoolean(true);
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server run, waiting connection");
            socket = serverSocket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client connected");
            out.writeUTF("Hello from Server!");
            new Thread(() -> {
                try {
                    while (true) {
                        String inStr = in.readUTF();
                        if (inStr.equalsIgnoreCase("/end")) {
                            System.out.println("Client disconnected");
                            //connection.set(false);
                            out.writeUTF("Server: you disconnected!");
                            break;
                        }
                        out.writeUTF("Server: " + inStr);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
