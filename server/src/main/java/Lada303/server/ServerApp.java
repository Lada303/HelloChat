package Lada303.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class ServerApp {

    public static void main(String[] args) throws IOException {
        LogManager manager = LogManager.getLogManager();
        manager.readConfiguration(new FileInputStream("logging.properties"));
        new Server();
    }
}
