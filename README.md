1. Model
ClientRequestHandler: Xử lý giao tiếp với server, gửi yêu cầu và nhận phản hồi.
SocketManager: Quản lý kết nối socket đến server.
UserDAO: Quản lý dữ liệu người dùng, bao gồm đăng ký và đăng nhập.
MessageDAO: Quản lý dữ liệu tin nhắn, bao gồm lưu trữ và truy xuất lịch sử hội thoại.
2. View
LoginController: Quản lý màn hình đăng nhập, xử lý đầu vào của người dùng cho việc đăng nhập.
ChatController: Quản lý màn hình chat, xử lý đầu vào của người dùng cho việc gửi tin nhắn và file.
3. Controller
LoginController: Hoạt động như một controller cho view đăng nhập, tương tác với ClientRequestHandler để gửi yêu cầu đăng nhập.
ChatController: Hoạt động như một controller cho view chat, tương tác với ClientRequestHandler để gửi tin nhắn và file.
ServerController: Quản lý server, khởi tạo và điều khiển các kết nối client.
Mối quan hệ giữa các lớp
LoginController và ChatController đều sử dụng ClientRequestHandler để giao tiếp với server.
ClientRequestHandler sử dụng SocketManager để lấy kết nối socket.
FXMLLoader được sử dụng trong LoginController và ChatController để chuyển đổi giữa các view khác nhau (đăng nhập, đăng ký, chat).
ServerSocketHandler lắng nghe các kết nối client và tạo một ClientHandler mới cho mỗi client.
ClientHandler xử lý các yêu cầu từ client (đăng nhập, đăng ký, nhắn tin, chuyển file).
Quy trình hoạt động và truyền dữ liệu
Khởi tạo Server:  
ServerController khởi tạo server và tạo một luồng ServerSocketHandler để lắng nghe các kết nối client.
Kết nối và xử lý Client:  
ServerSocketHandler chấp nhận kết nối client và tạo một ClientHandler mới cho mỗi client.
ClientHandler xử lý các yêu cầu từ client như đăng nhập, đăng ký, gửi tin nhắn, và chuyển file.
Đăng nhập và đăng ký Client:  
ClientHandler sử dụng UserDAO để xác thực người dùng và cập nhật danh sách người dùng trực tuyến.
Xử lý tin nhắn:  
ClientHandler sử dụng MessageDAO để lưu trữ tin nhắn vào cơ sở dữ liệu và gửi tin nhắn đến người nhận nếu họ đang trực tuyến.
Chuyển file:
ClientHandler nhận metadata và dữ liệu file từ client, lưu trữ file trên server và gửi file đến người nhận nếu họ đang trực tuyến.
