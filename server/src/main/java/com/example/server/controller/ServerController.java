package com.example.server.controller;

import com.example.server.dao.UserDAO;
import com.example.server.handler.ServerSocketHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ServerController {

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Button startButton;

    @FXML
    private ListView<String> clientsListView;

    @FXML
    private TextArea logTextArea;

    private ServerSocketHandler serverSocketHandler;
    private UserDAO userDAO; // Thêm biến UserDAO

    @FXML
    public void initialize() {
        // Khởi tạo UserDAO
        userDAO = new UserDAO();
    }

    @FXML
    private void handleStartServer() {
        String ip = ipField.getText();
        int port = Integer.parseInt(portField.getText());

        // Truyền thêm userDAO vào constructor của ServerSocketHandler
        serverSocketHandler = new ServerSocketHandler(ip, port, this, userDAO);
        new Thread(serverSocketHandler).start();
        updateLog("Server started on " + ip + ":" + port);
    }

    public void updateClientList(String clientInfo) {
        Platform.runLater(() -> clientsListView.getItems().add(clientInfo));
    }

    public void removeClientFromList(String clientInfo) {
        Platform.runLater(() -> clientsListView.getItems().remove(clientInfo));
    }

    public void updateLog(String message) {
        Platform.runLater(() -> logTextArea.appendText(message + "\n"));
    }
}
