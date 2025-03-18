module com.example.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.server.controller to javafx.fxml;
    exports com.example.server;
}
