package com.example.server.dao;

import com.example.server.model.Message;
import com.example.server.model.Conversation;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // Lưu tin nhắn mới
    public boolean saveMessage(int conversationId, int senderId, String messageContent) {
        String insertMessageSql = "INSERT INTO messages (conversation_id, sender_id, message) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement insertMessageStmt = conn.prepareStatement(insertMessageSql)) {

                // Thêm tin nhắn vào bảng messages
                insertMessageStmt.setInt(1, conversationId);
                insertMessageStmt.setInt(2, senderId);
                insertMessageStmt.setString(3, messageContent);
                insertMessageStmt.executeUpdate();

                conn.commit(); // Commit transaction nếu thành công
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Rollback nếu có lỗi
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // Đặt lại chế độ auto-commit
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Tìm cuộc hội thoại giữa hai người dùng dựa vào user1Id và user2Id
    public Integer findConversationIdByUserIds(int user1Id, int user2Id) {
        String sql = "SELECT id FROM conversations WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Trả về conversationId nếu cuộc hội thoại tồn tại
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu cuộc hội thoại không tồn tại
    }

    // Lấy lịch sử tin nhắn giữa hai người dùng theo conversationId
    public List<Message> getMessageHistory(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conversationId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getInt("id"),
                        rs.getInt("conversation_id"),
                        rs.getInt("sender_id"),
                        rs.getString("message"),
                        rs.getTimestamp("sent_at").toLocalDateTime()
                );
                messages.add(message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public int getOrCreateConversation(int user1Id, int user2Id) {
        String selectSql = "SELECT id FROM conversations WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";
        String insertSql = "INSERT INTO conversations (user1_id, user2_id, created_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Kiểm tra sự tồn tại của user1Id và user2Id trong bảng users
            if (!userExists(conn, user1Id) || !userExists(conn, user2Id)) {
                System.out.println("One of the users does not exist.");
                return -1; // Trả về -1 nếu một trong hai user không tồn tại
            }

            // Kiểm tra xem cuộc hội thoại đã tồn tại chưa
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, user1Id);
                selectStmt.setInt(2, user2Id);
                selectStmt.setInt(3, user2Id);
                selectStmt.setInt(4, user1Id);

                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id"); // Trả về conversationId nếu đã tồn tại
                }
            }

            // Tạo cuộc hội thoại mới nếu chưa tồn tại
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, user1Id);
                insertStmt.setInt(2, user2Id);
                insertStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Trả về conversationId mới
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu không thành công
    }

    // Hàm kiểm tra user có tồn tại trong bảng users hay không
    private boolean userExists(Connection conn, int userId) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Trả về true nếu user tồn tại
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
