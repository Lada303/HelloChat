package lada303.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea chatText;

    @FXML
    private TextField sendText;

    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            openConnection();
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("Server is not available or already connect\n");
            chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    private void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        connection = true;
        new Thread(() -> {
            try {
                while (connection) {
                    String inStr = in.readUTF();
                    chatText.appendText(inStr + "\n");
                    if (inStr.equalsIgnoreCase("Server: you disconnected!")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

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

    @FXML
    protected void sendMessage() {
        if (socket == null || socket.isClosed()) {
            chatText.appendText("No connection. Message don't sending.\n");
        } else if (!sendText.getText().isEmpty() && !sendText.getText().equals(" ")) {
            if (sendText.getText().equals("/end")) {
                chatText.appendText("/end\n");
                connection = false;
            }
            try {
                out.writeUTF(sendText.getText());
                sendText.setText("");
            } catch (IOException e) {
                //e.printStackTrace();
                chatText.appendText("Message sending error\n");
                chatText.appendText("(Exp: " + e.getMessage() + ")\n");
            }
        }
        sendText.requestFocus();
    }

    @FXML
    public void clickStartConnection() {
        try {
            openConnection();
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("Server is not available or already connect\n");
            chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    @FXML
    public void clickCloseConnection() {
        try {
            out.writeUTF("/end");
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    @FXML
    public void clickClose(ActionEvent actionEvent) {
        clickCloseConnection();
        Platform. runLater(() -> {
            Stage stage = (Stage) chatText.getScene().getWindow();
            stage.close();
        });
    }

    @FXML
    public void clickChangeTheme(ActionEvent actionEvent) {
        //MenuItem menuItem = (MenuItem) actionEvent.getSource();
        String cssFile = ((MenuItem) actionEvent.getSource()).getText().toLowerCase() + ".css";
        Platform. runLater(() -> {
            chatText.getScene().getStylesheets().clear();
            chatText.getScene().getStylesheets().add(cssFile);
            Stage stage = (Stage) chatText.getScene().getWindow();
            stage.setScene(chatText.getScene());
        });
    }
}