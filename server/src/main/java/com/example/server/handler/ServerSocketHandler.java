package com.example.server.handler;

import com.example.server.controller.ServerController;
import com.example.server.dao.UserDAO;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

public class ServerSocketHandler implements Runnable {
    private final String ip;
    private final int port;
    private final ServerController controller;
    private final UserDAO userDAO;
    private ServerSocket serverSocket;

    // Map lưu trữ userId và socket để quản lý user đang online
    private final Map<Integer, Socket> onlineUsers = new ConcurrentHashMap<>();

    public ServerSocketHandler(String ip, int port, ServerController controller, UserDAO userDAO) {
        this.ip = ip;
        this.port = port;
        this.controller = controller;
        this.userDAO = userDAO;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                String clientInfo = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
                controller.updateClientList(clientInfo);
                controller.updateLog("Client connected: " + clientInfo);

                // Tạo một ClientHandler mới và truyền userDAO cùng danh sách onlineUsers
                new Thread(new ClientHandler(clientSocket, controller, userDAO, onlineUsers)).start();
            }
        } catch (IOException e) {
            controller.updateLog("Error: " + e.getMessage());
        }
    }

    public Map<Integer, Socket> getOnlineUsers() {
        return onlineUsers;
    }
}

