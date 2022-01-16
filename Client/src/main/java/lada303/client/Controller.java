package lada303.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox logBox;
    @FXML
    private HBox msgBox;
    @FXML
    private TextArea chatText;
    @FXML
    private TextField sendText;

    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8189;
    private final int SIZE_LOGIN = 3;
    private final int SIZE_PASS = 3;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    //private boolean isAuthenticated;
    private String userNick;
    private Stage stage;

    private void setAuthenticated(boolean authenticated) {
        //this.isAuthenticated = authenticated;
        msgBox.setManaged(authenticated);
        msgBox.setVisible(authenticated);
        logBox.setManaged(!authenticated);
        logBox.setVisible(!authenticated);
        if(!authenticated) {
            userNick = "";
        }
        changeTitle();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform. runLater(() -> {
            stage = (Stage) chatText.getScene().getWindow();
            stage.setOnCloseRequest(windowEvent -> clickClose());
        });
        setAuthenticated(false);
    }

    private boolean isConnected() {
        return  !(socket == null || socket.isClosed());
    }

    private void startConnection() {
        try {
            if (!isConnected()) {
                openConnection();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("Server is not available\n");
            //chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    private void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (isConnected()) {
                    String inStr = in.readUTF();
                    chatText.appendText(inStr + "\n");
                    if (inStr.startsWith("Server:")) {
                        if (inStr.contains("Server: you authenticated")) {
                            userNick = inStr.split(" ")[4];
                            setAuthenticated(true);
                            continue;
                        }
                        if (inStr.equalsIgnoreCase("Server: you disconnected!")) {
                            setAuthenticated(false);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                chatText.appendText("(Exp: " + e.getMessage() + ")\n");
            } finally {
                closeConnection();
                setAuthenticated(false);
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
    protected void clickNewAuthentication() {
        if (!isConnected()) {
            setAuthenticated(false);
            return;
        }
        try {
            out.writeUTF("/end");
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    @FXML
    protected void clickClose() {
        clickNewAuthentication();
        Platform. runLater(() -> stage.close());
    }

    @FXML
    protected void clickChangeTheme(ActionEvent actionEvent) {
        String cssFile = ((MenuItem) actionEvent.getSource()).getText().toLowerCase() + ".css";
        Platform. runLater(() -> {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add(cssFile);
        });
    }

    @FXML
    protected void clickSendLogInfo() {
        startConnection();
        if (!isConnected()) {
            return;
        }
        String userLogin = loginField.getText().trim();
        String userPassword = passwordField.getText().trim();
        if (userLogin.length() < SIZE_LOGIN || userPassword.length() < SIZE_PASS) {
            chatText.appendText(String.format("Wrong login (min %d) or password (min %d) length\n",
                    SIZE_LOGIN, SIZE_PASS));
            return;
        }
        String logMsg = String.format("/auth %s %s", userLogin, userPassword);
        try {
            out.writeUTF(logMsg);
            passwordField.setText("");
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("Message sending error\n");
            //chatText.appendText("(Exp: " + e.getMessage() + ")\n");
        }
    }

    @FXML
    protected void clickSendMessage() {
        if (!isConnected()) {
            chatText.appendText("No connection. Message don't sending.\n");
        } else if (!sendText.getText().isEmpty() && !sendText.getText().equals(" ")) {
            try {
                out.writeUTF(sendText.getText());
                sendText.setText("");
            } catch (IOException e) {
                //e.printStackTrace();
                chatText.appendText("Message sending error\n");
                //chatText.appendText("(Exp: " + e.getMessage() + ")\n");
            }
        }
        sendText.requestFocus();
    }

    protected void changeTitle() {
        Platform. runLater(() -> stage.setTitle(HelloChatApp.CHAT_TITLE + " - " + userNick));
    }
}