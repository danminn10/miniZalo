package com.example.client.controller;

import com.example.client.ClientRequestHandler;
import com.example.client.SocketManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class RegisterController {

    @FXML
    private TextField fullnameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    private ClientRequestHandler requestHandler;

    @FXML
    public void initialize() {
        try {
            Socket socket = SocketManager.getInstance("127.0.0.1", 12345).getSocket();
            requestHandler = new ClientRequestHandler(socket);
            System.out.println("Connected to server for registration");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to server for registration");
        }
    }

    @FXML
    private void handleRegister() {
        String fullname = fullnameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("Fullname, Username hoặc Password không được để trống");
        } else {
            // Gửi yêu cầu đăng ký lên server
            if (requestHandler != null) {
                String response = requestHandler.register(username, password, fullname);
                System.out.println("Server response: " + response);

                if (response.startsWith("SUCCESS")) {
                    System.out.println("Đăng ký thành công với Fullname: " + fullname);
                    switchToLogin();
                } else {
                    System.out.println("Đăng ký thất bại: " + response);
                }
            } else {
                System.out.println("RequestHandler chưa được khởi tạo. Không thể gửi yêu cầu đăng ký.");
            }
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/Login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Login");
            stage.show();

            System.out.println("Chuyển sang màn hình đăng nhập...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
