package lada303.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegController {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea textArea;
    @FXML
    public VBox box;
    @FXML
    public Button btnReg;
    @FXML
    public Button btnChangeNick;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void clickBtn(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nick = nicknameField.getText().trim();
        if (controller.isLoginPasswordAvailable(login, password)) {
            textArea.appendText(String.format("Wrong login (min %d) or password (min %d) length\n",
                    controller.SIZE_LOGIN, controller.SIZE_PASS));
            return;
        }
        if (actionEvent.getSource().equals(btnReg)) {
            controller.sendRegInfo("/reg",login, password, nick);
            return;
        }
        if (actionEvent.getSource().equals(btnChangeNick)) {
            controller.sendRegInfo("/chg", login, password, nick);
            return;
        }
    }

    @FXML
    public void clickEnterByElement(ActionEvent actionEvent) {
        int ind = box.getChildren().indexOf(actionEvent.getSource());
        box.getChildren().get(ind + 2).requestFocus();
    }

    public Button getBtnReg() {
        return btnReg;
    }

    public Button getBtnChangeNick() {
        return btnChangeNick;
    }

    public void regStatus(String result) {
        if (result.equals("/regOk")) {
            textArea.appendText("Server: you have been registered\n");
        } else {
            textArea.appendText("Server: Registration failed. Login or nickname taken\n");
        }
    }

    public void chgStatus(String result) {
        if (result.equals("/chgOk")) {
            textArea.appendText("Server: you nick have been changed\n");
        } else {
            textArea.appendText("Server: Changed failed. Login or password have mistakes\n");
        }
    }

}
