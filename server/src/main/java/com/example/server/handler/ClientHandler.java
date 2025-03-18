package com.example.server.handler;

import com.example.server.controller.ServerController;
import com.example.server.dao.MessageDAO;
import com.example.server.dao.UserDAO;
import com.example.server.model.Message;
import com.example.server.model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ServerController controller;
    private final UserDAO userDAO;
    private final Map<Integer, Socket> onlineUsers;
    private int userId;

    private long fileSize;
    private int senderId;
    private String fileName;

    public ClientHandler(Socket clientSocket, ServerController controller, UserDAO userDAO, Map<Integer, Socket> onlineUsers) {
        this.clientSocket = clientSocket;
        this.controller = controller;
        this.userDAO = userDAO;
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = reader.readLine()) != null) {
                String[] parts = request.split(":");
                String action = parts[0];

                if ("LOGIN".equalsIgnoreCase(action)) {
                    String username = parts[1];
                    String password = parts[2];
                    handleLogin(username, password, writer);
                } else if ("LOGOUT".equalsIgnoreCase(action)) {
                    handleLogout();
                } else if ("REGISTER".equalsIgnoreCase(action)) {
                    String username = parts[1];
                    String password = parts[2];
                    String fullname = parts[3];
                    handleRegister(username, password, fullname, writer);
                } else if ("GET_USERS".equalsIgnoreCase(action)) {
                    handleGetUsers(writer);
                } else if ("SEND_MESSAGE".equalsIgnoreCase(action)) {
                    int recipientId = Integer.parseInt(parts[1]);
                    String message = parts[2];
                    handleSendMessage(recipientId, message);
                } else if ("GET_CONVERSATION_HISTORY".equalsIgnoreCase(action)) {
                    int recipientId = Integer.parseInt(parts[1]);
                    handleGetConversationHistory(recipientId, writer);
                }
                else if ("SEND_FILE_METADATA".equalsIgnoreCase(action)) {
                    handleFileMetadata(parts);
                } else if ("SEND_FILE_DATA".equalsIgnoreCase(action)) {
                    long fileSize = Long.parseLong(parts[1]);
                    handleFileData();
                }else {
                    writer.println("FAILURE: Unknown action.");
                }
            }
        } catch (IOException e) {
            controller.updateLog("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                controller.removeClientFromList(clientSocket.getInetAddress().toString());
                controller.updateLog("Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                controller.updateLog("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleLogin(String username, String password, PrintWriter writer) {
        User user = userDAO.loginUser(username, password);
        if (user != null) {
            this.userId = user.getId();
            onlineUsers.put(userId, clientSocket);
            writer.println("SUCCESS: Login successful. Welcome, " + user.getFullname());
            controller.updateLog("User logged in: " + username);
        } else {
            writer.println("FAILURE: Invalid username or password.");
        }
    }

    private void handleLogout() {
        // Xóa user khỏi danh sách online nếu user đang đăng nhập
        if (userId != 0) {
            onlineUsers.remove(userId);
            controller.updateLog("User logged out: " + userId);
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            controller.updateLog("Error closing client socket: " + e.getMessage());
        }
    }

    private void handleRegister(String username, String password, String fullname, PrintWriter writer) {
        boolean registered = userDAO.registerUser(new User(0, username, password, fullname));
        if (registered) {
            writer.println("SUCCESS: Registration successful.");
            controller.updateLog("User registered: " + username);
        } else {
            writer.println("FAILURE: Registration failed.");
        }
    }

    private void handleGetUsers(PrintWriter writer) {
        List<User> users = userDAO.getAllUsers();
        if (users != null && !users.isEmpty()) {
            // Lọc bỏ chính user đang request khỏi danh sách
            String usersList = users.stream()
                    .filter(user -> user.getId() != this.userId)  // Bỏ qua user có ID trùng với userId của người request
                    .map(user -> user.getId() + "|" + user.getUsername() + " (" + user.getFullname() + ")")
                    .collect(Collectors.joining(", "));

            writer.println("USERS_LIST:" + usersList);
            controller.updateLog("Sent filtered user list to client.");
        } else {
            writer.println("FAILURE: No users found.");
        }
    }

    private void handleSendMessage(int recipientId, String message) {
        try {
            // Kiểm tra hoặc tạo cuộc hội thoại giữa người gửi (userId) và người nhận (recipientId)
            MessageDAO messageDAO = new MessageDAO();
            int conversationId = messageDAO.getOrCreateConversation(userId, recipientId);

            if (conversationId != -1) {
                // Lưu tin nhắn vào cơ sở dữ liệu
                boolean isMessageSaved = messageDAO.saveMessage(conversationId, userId, message);
                if (isMessageSaved) {
                    Socket recipientSocket = onlineUsers.get(recipientId);
                    if (recipientSocket != null) {
                        // Gửi tin nhắn đến người nhận nếu họ đang online
                        PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream(), true);
                        recipientWriter.println("MESSAGE_FROM:" + userId + ":" + message); // Gửi tin nhắn với ID người gửi
                        controller.updateLog("Message sent from " + userId + " to " + recipientId);
                    } else {
                        controller.updateLog("User " + recipientId + " is not online. Message could not be delivered.");
                    }
                } else {
                    controller.updateLog("Failed to save message in database for conversation " + conversationId);
                }
            } else {
                controller.updateLog("Failed to create or find conversation between " + userId + " and " + recipientId);
            }
        } catch (IOException e) {
            controller.updateLog("Error sending message to " + recipientId + ": " + e.getMessage());
        }
    }

    // Gửi phản hồi với lịch sử hội thoại
    private void handleGetConversationHistory(int recipientId, PrintWriter writer) {
        try {
            if (recipientId <= 0) {
                writer.println("FAILURE: Invalid recipient ID.");
                writer.flush();
                controller.updateLog("Failed to retrieve conversation history due to invalid recipient ID.");
                return;
            }

            MessageDAO messageDAO = new MessageDAO();
            int conversationId = messageDAO.getOrCreateConversation(userId, recipientId);

            if (conversationId != -1) {
                List<Message> messages = messageDAO.getMessageHistory(conversationId);

                if (!messages.isEmpty()) {
                    String history = messages.stream()
                            .map(msg -> msg.getSenderId() + "|" + msg.getMessage() + "|" + msg.getSentAt())
                            .collect(Collectors.joining(";"));
                    writer.println("CONVERSATION_HISTORY:" + history);
                    writer.flush();
                    controller.updateLog("Sent conversation history to user " + userId);
                } else {
                    writer.println("FAILURE: No messages found.");
                    writer.flush();
                    controller.updateLog("No messages found for conversation between user " + userId + " and recipient " + recipientId);
                }
            } else {
                writer.println("FAILURE: Conversation not found between users.");
                writer.flush();
                controller.updateLog("Failed to retrieve conversation history: conversation not found between user " + userId + " and recipient " + recipientId);
            }
        } catch (Exception e) {
            controller.updateLog("Unexpected error while retrieving conversation history: " + e.getMessage());
            e.printStackTrace();
            writer.println("FAILURE: Unexpected error occurred.");
            writer.flush();
        }
    }

    private void handleFileMetadata(String[] parts) {
        this.senderId = Integer.parseInt(parts[1]);
        this.fileName = parts[2];
        this.fileSize = Long.parseLong(parts[3]);

        controller.updateLog("Receiving file metadata for '" + fileName + "' of size " + fileSize + " bytes from user " + senderId);

        // Bắt đầu đọc file ngay sau khi nhận metadata
        handleFileData();
    }

    private void handleFileData() {
        try {
            byte[] fileData = new byte[(int) fileSize];
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataInputStream.readFully(fileData);

            // Kiểm tra và tạo thư mục 'files' nếu chưa tồn tại
            File dir = new File("files");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Lưu file vào thư mục 'files' với tên file gốc
            FileOutputStream fileOutputStream = new FileOutputStream("files/received_" + fileName);
            fileOutputStream.write(fileData);
            fileOutputStream.close();

            controller.updateLog("File received successfully, size: " + fileSize + " bytes");

            // Sau khi nhận file xong, gửi file cho người nhận
            sendFileToRecipient(senderId, fileName, senderId);

        } catch (IOException e) {
            controller.updateLog("Error receiving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendFileToRecipient(int recipientId, String fileName, int senderId) {
        Socket recipientSocket = onlineUsers.get(recipientId);

        if (recipientSocket != null) {
            try {
                // Gửi metadata của file tới client, bao gồm cả senderId
                PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream(), true);
                File file = new File("files/received_" + fileName);
                long fileSize = file.length();
                recipientWriter.println("FILE_TRANSFER:" + senderId + ":" + fileName + ":" + fileSize);

                controller.updateLog("Sending file metadata to recipient " + recipientId + " from sender " + senderId + " - File: " + fileName + ", Size: " + fileSize);

                // Gửi dữ liệu file
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                OutputStream recipientOutputStream = recipientSocket.getOutputStream();
                int bytesRead;
                long totalBytesSent = 0;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    recipientOutputStream.write(buffer, 0, bytesRead);
                    totalBytesSent += bytesRead;
                    controller.updateLog("Sent " + bytesRead + " bytes to recipient " + recipientId);
                }

                recipientOutputStream.flush();
                fileInputStream.close();

                controller.updateLog("File " + fileName + " sent successfully to user " + recipientId + ". Total bytes sent: " + totalBytesSent);

                // Lưu thông tin file vào database như là một tin nhắn
                saveFileMessageToDatabase(senderId, recipientId, fileName);
            } catch (IOException e) {
                controller.updateLog("Error sending file to recipient: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            controller.updateLog("User " + recipientId + " is not online. File could not be delivered.");
        }
    }


    private void saveFileMessageToDatabase(int senderId, int recipientId, String fileName) {
        MessageDAO messageDAO = new MessageDAO();
        String fileMessage = "[File Sent] " + fileName;

        // Lấy hoặc tạo conversation ID giữa người gửi và người nhận
        int conversationId = messageDAO.getOrCreateConversation(senderId, recipientId);

        if (conversationId == -1) {
            controller.updateLog("Failed to retrieve or create conversation for file message.");
            return; // Dừng lại nếu không lấy được conversationId
        }

        // Lưu tin nhắn dưới dạng thông tin file vào cuộc hội thoại
        boolean isMessageSaved = messageDAO.saveMessage(conversationId, senderId, fileMessage);

        if (isMessageSaved) {
            controller.updateLog("File message saved to database: " + fileMessage);
        } else {
            controller.updateLog("Failed to save file message to database for file: " + fileName);
        }
    }


}
