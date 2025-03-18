package com.example.client.controller;

import com.example.client.ClientRequestHandler;
import com.example.client.ServerListener;
import com.example.client.SocketManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatController {

    @FXML
    private Label chatWithLabel;

    @FXML
    private VBox messageContainer;

    @FXML
    private TextField messageField;

    private String currentUser = "You";
    private int recipientId;
    private ClientRequestHandler requestHandler;



    public ChatController() {
        try {
            Socket socket = SocketManager.getInstance("127.0.0.1", 12345).getSocket();
            requestHandler = new ClientRequestHandler(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChatWithUser(int recipientId, String username) {
        this.recipientId = recipientId;
        chatWithLabel.setText("Chat với: " + username);
    }

    public int getRecipientId() {
        return recipientId;
    }

    private void loadChatHistory(String username) {
        addMessage("Hello!", "You", LocalTime.now().minusMinutes(10));
        addMessage("Hi there!", username, LocalTime.now().minusMinutes(9));
    }

    public void addMessage(String message, String sender, LocalTime time) {
        HBox messageBox = new HBox();
        VBox messageContent = new VBox();

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle("-fx-background-color: #e1ffc7; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Label timeLabel = new Label(time.format(formatter));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        messageContent.getChildren().addAll(messageLabel, timeLabel);

        if (sender.equals(currentUser)) {
            messageBox.setStyle("-fx-alignment: CENTER_RIGHT;");
            messageLabel.setStyle("-fx-background-color: #d1e7ff;");
        } else {
            messageBox.setStyle("-fx-alignment: CENTER_LEFT;");
            messageLabel.setStyle("-fx-background-color: #e1ffc7;");
        }

        messageBox.getChildren().add(messageContent);
        messageContainer.getChildren().add(messageBox);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && requestHandler != null) {
            requestHandler.sendMessage(recipientId, message);
            addMessage(message, currentUser, LocalTime.now());
            messageField.clear();
        }
    }

    public void displayChatHistory(String history) {
        if (history.startsWith("CONVERSATION_HISTORY:")) {
            history = history.substring("CONVERSATION_HISTORY:".length());

            String[] messages = history.split(";");
            for (String msg : messages) {
                String[] parts = msg.split("\\|");
                if (parts.length == 3) {
                    try {
                        int senderId = Integer.parseInt(parts[0]);
                        String messageContent = parts[1];
                        // Sử dụng LocalDateTime để parse và lấy thời gian
                        LocalDateTime dateTime = LocalDateTime.parse(parts[2]);
                        LocalTime time = dateTime.toLocalTime();

                        String sender = (senderId == this.recipientId) ? chatWithLabel.getText() : currentUser;
                        addMessage(messageContent, sender, time);
                    } catch (Exception e) {
                        System.out.println("Failed to parse message data: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Failed to retrieve chat history.");
        }
    }

    @FXML
    private void handleSendFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            requestHandler.sendFile(recipientId, file);
            // Hiển thị thông tin gửi file lên giao diện
            String fileMessage = "Đã gửi file: " + file.getName();
            addMessage(fileMessage, currentUser, LocalTime.now());
        }
    }
}
