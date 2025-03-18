module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.client;
    exports com.example.client.controller; // Xuất gói controller để các module khác có thể truy cập

    opens com.example.client to javafx.fxml;
    opens com.example.client.controller to javafx.fxml; // Mở gói controller cho FXML để có thể khởi tạo controller
}
