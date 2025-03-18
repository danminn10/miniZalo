
# AppChat - Ứng dụng nhắn tin đơn giản

AppChat là một ứng dụng nhắn tin đơn giản, cho phép người dùng đăng ký, đăng nhập, và nhắn tin với nhau. Ứng dụng hỗ trợ gửi tin nhắn văn bản và file, đồng thời lưu trữ lịch sử trò chuyện.

## Tính năng chính

- **Đăng ký và đăng nhập**: Người dùng có thể đăng ký tài khoản mới và đăng nhập vào hệ thống.
- **Nhắn tin**: Người dùng có thể nhắn tin với nhau thông qua giao diện trò chuyện.
- **Gửi file**: Người dùng có thể gửi file cho nhau, và file sẽ được lưu trữ trên máy chủ.
- **Lịch sử trò chuyện**: Lịch sử trò chuyện được lưu trữ và hiển thị khi người dùng mở lại cuộc trò chuyện.
- **Danh sách người dùng**: Hiển thị danh sách người dùng đang hoạt động trong hệ thống.

## Công nghệ sử dụng

- **Ngôn ngữ lập trình**: Java
- **Giao diện người dùng**: JavaFX
- **Cơ sở dữ liệu**: MySQL
- **Giao thức mạng**: Socket TCP/IP

## Cấu trúc project

### Client

- **`MainApp.java`**: Khởi chạy ứng dụng client và hiển thị màn hình đăng nhập.
- **`ClientRequestHandler.java`**: Xử lý các yêu cầu từ client đến server, bao gồm đăng nhập, đăng ký, gửi tin nhắn, và gửi file.
- **`ServerListener.java`**: Lắng nghe các phản hồi từ server, bao gồm tin nhắn mới, file nhận được, và lịch sử trò chuyện.
- **`SocketManager.java`**: Quản lý kết nối socket giữa client và server.
- **`ChatController.java`**: Điều khiển giao diện trò chuyện, bao gồm gửi tin nhắn và file.
- **`LoginController.java`**: Điều khiển giao diện đăng nhập và xử lý đăng nhập.
- **`RegisterController.java`**: Điều khiển giao diện đăng ký và xử lý đăng ký.
- **`MainController.java`**: Điều khiển giao diện chính, bao gồm danh sách người dùng và chuyển đổi giữa các màn hình.

### Server

- **`ServerMain.java`**: Khởi chạy server và hiển thị giao diện quản lý server.
- **`ServerSocketHandler.java`**: Xử lý kết nối từ client và quản lý các client đang kết nối.
- **`ClientHandler.java`**: Xử lý các yêu cầu từ client, bao gồm đăng nhập, đăng ký, gửi tin nhắn, và gửi file.
- **`DatabaseConnection.java`**: Quản lý kết nối đến cơ sở dữ liệu MySQL.
- **`UserDAO.java`**: Xử lý các thao tác liên quan đến người dùng trong cơ sở dữ liệu.
- **`MessageDAO.java`**: Xử lý các thao tác liên quan đến tin nhắn và cuộc hội thoại trong cơ sở dữ liệu.
- **`Conversation.java`**: Model đại diện cho một cuộc hội thoại giữa hai người dùng.
- **`Message.java`**: Model đại diện cho một tin nhắn trong cuộc hội thoại.
- **`User.java`**: Model đại diện cho một người dùng.

## Hướng dẫn cài đặt và chạy ứng dụng

### Yêu cầu hệ thống

- **Java Development Kit (JDK)**: Phiên bản 11 trở lên.
- **JavaFX**: Đã được cài đặt và cấu hình.
- **MySQL**: Cơ sở dữ liệu MySQL đã được cài đặt và cấu hình.

### Các bước chạy ứng dụng

1. **Clone repository**:
   ```bash
   git clone https://github.com/[your-username]/AppChat.git
   ```

2. **Cấu hình cơ sở dữ liệu**:
   - Tạo cơ sở dữ liệu `chat_app_javafx` trong MySQL.
   - Chạy các script SQL để tạo bảng `users`, `conversations`, và `messages`.

3. **Mở project bằng IDE**:
   - Mở project bằng IntelliJ IDEA hoặc Eclipse.
   - Đảm bảo rằng JavaFX đã được cấu hình trong IDE của bạn.

4. **Chạy server**:
   - Mở file `ServerMain.java` và chạy để khởi động server.

5. **Chạy client**:
   - Mở file `MainApp.java` và chạy để khởi động ứng dụng client.

6. **Đăng ký và đăng nhập**:
   - Trên màn hình đăng nhập, bạn có thể đăng ký tài khoản mới hoặc đăng nhập nếu đã có tài khoản.

7. **Nhắn tin và gửi file**:
   - Sau khi đăng nhập, bạn có thể chọn người dùng từ danh sách để bắt đầu trò chuyện và gửi file.

## Hướng dẫn đóng góp

Nếu bạn muốn đóng góp vào dự án, vui lòng làm theo các bước sau:

1. Fork repository.
2. Tạo branch mới cho tính năng của bạn (`git checkout -b feature/AmazingFeature`).
3. Commit các thay đổi của bạn (`git commit -m 'Add some AmazingFeature'`).
4. Push lên branch (`git push origin feature/AmazingFeature`).
5. Mở một Pull Request.

## Giấy phép

Dự án này được phân phối dưới giấy phép MIT. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.
