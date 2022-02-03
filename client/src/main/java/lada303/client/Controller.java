package lada303.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public MenuItem miChangeNick;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    public Button btnReg;
    @FXML
    private HBox logBox;
    @FXML
    private HBox msgBox;
    @FXML
    private TextArea chatText;
    @FXML
    public ListView<String> usersList;
    @FXML
    private TextField sendText;

    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 8189;
    protected final int SIZE_LOGIN = 3;
    protected final int SIZE_PASS = 3;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String userNick;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform. runLater(() -> {
            stage = (Stage) chatText.getScene().getWindow();
            stage.setOnCloseRequest(windowEvent -> clickClose());
        });
        setAuthenticated(false);
    }

    private void setAuthenticated(boolean authenticated) {
        miChangeNick.setVisible(authenticated);
        msgBox.setManaged(authenticated);
        msgBox.setVisible(authenticated);
        logBox.setManaged(!authenticated);
        logBox.setVisible(!authenticated);
        if(!authenticated) {
            userNick = "";
        }
        changeTitle();
    }

    private void changeTitle() {
        Platform. runLater(() -> stage.setTitle(HelloChatApp.CHAT_TITLE + " - " + userNick));
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
                M: while (isConnected()) {
                    String inStr = in.readUTF();
                    String[] tokens = inStr.split(" ");
                    if (inStr.startsWith("/")) {
                        switch (tokens[0]) {
                            case "/regOk":
                                regController.regStatus(tokens[0]);
                            case "/authOk":
                                userNick = tokens[1];
                                chatText.appendText("Server: you authenticated - " + userNick + "\n");
                                setAuthenticated(true);
                                continue;
                            case "/regNo":
                                regController.regStatus(tokens[0]);
                                continue;
                            case "/chgOk":
                                userNick = tokens[1];
                                chatText.appendText("Server: you changed nick - " + userNick + "\n");
                                setAuthenticated(true);
                            case "/chgNo":
                                regController.chgStatus(tokens[0]);
                                continue;
                            case "/clients":
                                Platform. runLater(() -> {
                                    usersList.getItems().clear();
                                    usersList.getItems().addAll(tokens);
                                    usersList.getItems().remove(tokens[0]);
                                });
                                continue;
                            case "/end":
                                Platform. runLater(() -> usersList.getItems().clear());
                                chatText.appendText("Server: you disconnected!\n");
                                clickNewAuthentication();
                                break M;
                        }
                    }
                    chatText.appendText(inStr + "\n");
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
        outMsg("/end");
    }

    private void outMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            //e.printStackTrace();
            chatText.appendText("Message sending error\n");
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
            if (regStage != null) {
                regStage.getScene().getStylesheets().clear();
                regStage.getScene().getStylesheets().add(cssFile);
            }
        });
    }

    @FXML
    protected void clickEnterByLoginOrPassword(ActionEvent actionEvent) {
        int ind = logBox.getChildren().indexOf(actionEvent.getSource());
        logBox.getChildren().get(ind + 1).requestFocus();
    }

    @FXML
    protected void clickLog() {
        startConnection();
        if (!isConnected()) {
            return;
        }
        if (isLoginPasswordAvailable(loginField.getText().trim(), passwordField.getText().trim())) {
            chatText.appendText(String.format("Wrong login (min %d) or password (min %d) length\n",
                    SIZE_LOGIN, SIZE_PASS));
            return;
        }
        outMsg(String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim()));
        passwordField.setText("");
    }

    protected boolean isLoginPasswordAvailable(String login, String pass) {
        return login.length() < SIZE_LOGIN || pass.length() < SIZE_PASS;
    }

    @FXML
    protected void clickReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        changeBtnReg(actionEvent.getSource().equals(btnReg));
        regStage.show();
    }

    private void changeBtnReg(boolean bln) {
        regController.getBtnReg().setVisible(bln);
        regController.getBtnReg().setManaged(bln);
        regController.getBtnChangeNick().setVisible(!bln);
        regController.getBtnChangeNick().setManaged(!bln);
    }

    @FXML
    protected void recipientSelection() {
        sendText.setText("/w " + usersList.getSelectionModel().getSelectedItem());
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
            }
        }
        sendText.requestFocus();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reg-view.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle(HelloChatApp.CHAT_TITLE + " registration");
            regStage.setScene(new Scene(root, 400, 300));
            if (stage.getScene().getStylesheets().size() != 0) {
                regStage.getScene().getStylesheets().add(stage.getScene().getStylesheets().get(0));
            }
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendRegInfo(String com, String login, String password, String nick) {
        startConnection();
        if (!isConnected()) {
            return;
        }
        outMsg(String.format("%s %s %s %s", com, login, password, nick));
    }
}