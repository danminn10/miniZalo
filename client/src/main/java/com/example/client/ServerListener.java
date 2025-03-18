package com.example.client;

import com.example.client.controller.ChatController;
import com.example.client.controller.MainController;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;

public class ServerListener implements Runnable {
    private final Socket socket;
    private final MainController mainController;
    private ChatController chatController;

    public ServerListener(Socket socket, MainController mainController) {
        this.socket = socket;
        this.mainController = mainController;
    }

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String response;
            while ((response = reader.readLine()) != null) {
                if (response.startsWith("MESSAGE_FROM:")) {
                    String[] parts = response.split(":");
                    int senderId = Integer.parseInt(parts[1]);
                    String message = parts[2];
                    handleIncomingMessage(senderId, message);
                }
                else if (response.startsWith("CONVERSATION_HISTORY:")) {
                    System.out.println("check: " + response);
                    handleChatHistory(response);
                }
                else if (response.startsWith("FILE_RECEIVED:")) {
                    String[] parts = response.split(":");
                    int senderId = Integer.parseInt(parts[1]);
                    String fileName = parts[2];

                    Platform.runLater(() -> {
                        if (chatController != null && chatController.getRecipientId() == senderId) {
                            chatController.addMessage("Đã nhận file: " + fileName, "User " + senderId, LocalTime.now());
                        } else {
                            mainController.highlightUser(senderId);
                        }
                    });
                }
                else if (response.startsWith("FILE_TRANSFER:")) {
                    String[] parts = response.split(":");
                    int senderId = Integer.parseInt(parts[1]);
                    String fileName = parts[2];
                    long fileSize = Long.parseLong(parts[3]);

                    receiveFile(fileName, fileSize, senderId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingMessage(int senderId, String message) {
        Platform.runLater(() -> {
            if (chatController != null && chatController.getRecipientId() == senderId) {
                chatController.addMessage(message, "User " + senderId, LocalTime.now());
            } else {
                mainController.highlightUser(senderId);
            }
        });
    }

    private void handleChatHistory(String historyResponse) {
        if (chatController != null) {
            Platform.runLater(() -> {
                // Xóa tiền tố "CONVERSATION_HISTORY:"
//                String history = historyResponse.substring("CONVERSATION_HISTORY:".length());
                chatController.displayChatHistory(historyResponse);
            });
        }
    }

    private void receiveFile(String fileName, long fileSize, int senderId) {
        try {
            // Tạo thư mục nếu chưa tồn tại
            File dir = new File("received_files");
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Directory 'received_files' created for storing received files.");
            }

            // Chuẩn bị lưu file nhận được
            File file = new File(dir, "received_" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            long totalBytesRead = 0;
            int bytesRead;

            System.out.println("Receiving file '" + fileName + "' with expected size: " + fileSize + " bytes.");

            while (totalBytesRead < fileSize) {
                bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                if (bytesRead == -1) break;

                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                System.out.println("Received " + bytesRead + " bytes. Total bytes received: " + totalBytesRead + "/" + fileSize);
            }

            fileOutputStream.close();

            if (totalBytesRead == fileSize) {
                System.out.println("File received and saved as: " + file.getAbsolutePath());

                // Hiển thị thông báo file đã nhận
                Platform.runLater(() -> {
                    if (chatController != null) {
                        chatController.addMessage("File received: " + fileName, "User " + senderId, LocalTime.now());
                    }
                });

            } else {
                System.out.println("Error: Expected " + fileSize + " bytes, but received " + totalBytesRead + " bytes.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
