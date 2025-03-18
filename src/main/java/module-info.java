module com.example.appchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.appchat to javafx.fxml;
    exports com.example.appchat;
}