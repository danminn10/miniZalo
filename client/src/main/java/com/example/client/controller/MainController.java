package com.example.client.controller;

import com.example.client.ClientRequestHandler;
import com.example.client.ServerListener;
import com.example.client.SocketManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    private ListView<Text> userListView;

    @FXML
    private ImageView settingsIcon;

    @FXML
    private ImageView logoutIcon;

    private ClientRequestHandler requestHandler;
    private ServerListener serverListener;
    private Map<Integer, Text> userTextMap = new HashMap<>();  // Lưu trữ Text của từng user theo ID

    @FXML
    public void initialize() {
        try {
            // Lấy socket từ SocketManager và khởi tạo ClientRequestHandler
            Socket socket = SocketManager.getInstance("127.0.0.1", 12345).getSocket();
            requestHandler = new ClientRequestHandler(socket);
            System.out.println("Reusing existing connection to server");

            // Tải danh sách người dùng
            loadUserList();

            // Khởi chạy ServerListener để lắng nghe tin nhắn từ server
            serverListener = new ServerListener(socket, this);
            new Thread(serverListener).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to server");
        }

        settingsIcon.setImage(new Image(getClass().getResource("/com/example/client/icons/settings.png").toExternalForm()));
        logoutIcon.setImage(new Image(getClass().getResource("/com/example/client/icons/logout.png").toExternalForm()));

        // Xử lý sự kiện khi nhấn vào tên người dùng trong danh sách
        userListView.setOnMouseClicked(event -> {
            Text selectedText = userListView.getSelectionModel().getSelectedItem();
            if (selectedText != null) {
                String selectedUser = selectedText.getText();
                openChatWithUser(selectedUser);
                selectedText.setFill(Color.BLACK);  // Xóa màu đỏ khi mở trò chuyện
            }
        });
    }

    private void loadUserList() {
        if (requestHandler != null) {
            String response = requestHandler.getUsersList();
            if (response.startsWith("USERS_LIST:")) {
                List<String> users = Arrays.asList(response.substring("USERS_LIST:".length()).split(", "));
                userListView.getItems().clear();

                for (String user : users) {
                    String[] parts = user.split("\\|");
                    if (parts.length == 2) {
                        int id = Integer.parseInt(parts[0]);
                        String usernameWithFullname = parts[1];

                        // Chỉ lấy tên đầy đủ từ chuỗi usernameWithFullname
                        String fullName = usernameWithFullname.substring(usernameWithFullname.indexOf('(') + 1, usernameWithFullname.indexOf(')'));

                        Text userText = new Text(fullName);
                        userTextMap.put(id, userText);  // Lưu Text vào map với ID user
                        userListView.getItems().add(userText);
                    }
                }
            } else {
                System.out.println("Failed to load user list: " + response);
            }
        }
    }

    public void highlightUser(int userId) {
        Text userText = userTextMap.get(userId);
        if (userText != null) {
            userText.setFill(Color.RED);  // Đổi màu đỏ để thông báo có tin nhắn mới
        }
    }

    private void openChatWithUser(String usernameWithFullname) {
        try {
            int recipientId = -1;
            for (Map.Entry<Integer, Text> entry : userTextMap.entrySet()) {
                if (entry.getValue().getText().equals(usernameWithFullname)) {
                    recipientId = entry.getKey();
                    break;
                }
            }
            if (recipientId == -1) {
                System.out.println("User ID not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/ChatView.fxml"));
            Parent chatRoot = loader.load();

            ChatController chatController = loader.getController();
            chatController.setChatWithUser(recipientId, usernameWithFullname);

            serverListener.setChatController(chatController);

            Socket socket = SocketManager.getInstance("127.0.0.1", 12345).getSocket();
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("GET_CONVERSATION_HISTORY:" + recipientId);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(chatRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void showSettings() {
        loadView("/com/example/client/Settings.fxml");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logout button clicked.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/client/Login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            contentPane.getChildren().clear();
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
