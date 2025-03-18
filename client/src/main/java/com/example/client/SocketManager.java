package com.example.client;

import java.io.IOException;
import java.net.Socket;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;

    private SocketManager(String serverIp, int serverPort) throws IOException {
        this.socket = new Socket(serverIp, serverPort);
    }

    public static SocketManager getInstance(String serverIp, int serverPort) throws IOException {
        if (instance == null) {
            instance = new SocketManager(serverIp, serverPort);
        }
        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

    public void closeSocket() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            instance = null;  // Allow reconnection if needed
        }
    }
}
