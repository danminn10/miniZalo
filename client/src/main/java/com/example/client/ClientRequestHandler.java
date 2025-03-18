package com.example.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ClientRequestHandler {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final OutputStream outputStream;


    public ClientRequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.outputStream = socket.getOutputStream();
    }

    // Gửi yêu cầu đăng nhập
    public String login(String username, String password) {
        writer.println("LOGIN:" + username + ":" + password);
        return receiveResponse();
    }

    // Gửi yêu cầu đăng ký
    public String register(String username, String password, String fullname) {
        writer.println("REGISTER:" + username + ":" + password + ":" + fullname);
        return receiveResponse();
    }

    // Gửi yêu cầu lấy danh sách người dùng
    public String getUsersList() {
        writer.println("GET_USERS");
        return receiveResponse();
    }

    // Gửi tin nhắn đến server
    public void sendMessage(int recipientId, String message) {
        writer.println("SEND_MESSAGE:" + recipientId + ":" + message);
    }

    // Gửi metadata của file
    public void sendFileMetadata(int recipientId, String fileName, long fileSize) {
        writer.println("SEND_FILE_METADATA:" + recipientId + ":" + fileName + ":" + fileSize);
    }

    public void sendFile(int recipientId, File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            String fileName = file.getName();
            long fileSize = file.length();

            // Gửi metadata của file
            writer.println("SEND_FILE_METADATA:" + recipientId + ":" + fileName + ":" + fileSize);
            writer.flush();
            System.out.println("File metadata sent for file: " + fileName + " (size: " + fileSize + " bytes)");

            // Gửi dữ liệu file ngay sau khi gửi metadata
            outputStream.write(fileData);
            outputStream.flush();

            System.out.println("File data sent for: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // Nhận phản hồi từ server
    private String receiveResponse() {
        try {
            System.out.println("Waiting for response from server...");
            String response = reader.readLine();
            if (response == null || response.isEmpty()) {
                throw new IOException("No response or empty response from server.");
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error receiving response from server.";
        }
    }

    // Đóng kết nối
    public void close() {
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
