package lada303.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    public VBox box;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void clickBtnReg() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nick = nicknameField.getText().trim();
        if (controller.isLoginPasswordAvailable(login, password)) {
            textArea.appendText(String.format("Wrong login (min %d) or password (min %d) length\n",
                    controller.SIZE_LOGIN, controller.SIZE_PASS));
            return;
        }
        controller.sendRegInfo(login, password, nick);
    }
    @FXML
    protected void clickEnterByElement(ActionEvent actionEvent) {
        int ind = box.getChildren().indexOf(actionEvent.getSource());
        box.getChildren().get(ind + 2).requestFocus();
    }

    public void regStatus(String result) {
        if (result.equals("/regOk")) {
            textArea.appendText("Server: you have been registered\n");
        } else {
            textArea.appendText("Registration failed. Login or nickname taken\n");
        }
    }

}
