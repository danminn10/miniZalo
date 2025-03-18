package com.example.client.controller;

import com.example.client.ClientRequestHandler;
import com.example.client.SocketManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private ClientRequestHandler requestHandler;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerButton;

    @FXML
    public void initialize() {
        try {
            // Khởi tạo socket kết nối đến server
            SocketManager socketManager = SocketManager.getInstance("127.0.0.1", 12345);
            requestHandler = new ClientRequestHandler(socketManager.getSocket());
            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to server");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username hoặc Password không được để trống");
        } else {
            if (requestHandler != null) {
                // Gửi yêu cầu đăng nhập lên server
                String response = requestHandler.login(username, password);
                System.out.println("Server response: " + response);

                // Kiểm tra nếu phản hồi bắt đầu bằng "SUCCESS"
                if (response.startsWith("SUCCESS")) {
                    System.out.println("Đăng nhập thành công");
                    openMainScreen();
                } else {
                    System.out.println("Đăng nhập thất bại: " + response);
                }
            } else {
                System.out.println("RequestHandler chưa được khởi tạo. Không thể gửi yêu cầu đăng nhập.");
            }
        }
    }

    @FXML
    private void switchToRegister() {
        try {
            // Tải file FXML của màn hình đăng ký
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/Register.fxml"));
            Parent registerRoot = loader.load();

            // Lấy Stage hiện tại và thay đổi Scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(registerRoot));
            stage.setTitle("Register");
            stage.show();

            System.out.println("Chuyển sang màn hình đăng ký...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMainScreen() {
        try {
            // Tải file FXML của màn hình chính
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/Main.fxml"));
            Parent mainRoot = loader.load();

            // Lấy Stage hiện tại và thay đổi Scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(mainRoot));
            stage.setTitle("Chat Application - Main");
            stage.show();

            System.out.println("Chuyển sang màn hình chính...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
