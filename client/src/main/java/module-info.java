module lada303.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens lada303.client to javafx.fxml;
    exports lada303.client;
}