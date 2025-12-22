# 4.1.24. Manage Campaign – Quản lý chiến dịch khuyến mãi

## Use Case ID
UC-24

## Use Case Name
Manage Campaign

## Actor(s)
- ADMIN

## Trigger
Admin chọn chức năng **“Quản lý khuyến mãi”** → tab **“Chiến dịch khuyến mãi”** trên giao diện quản trị.

## Description
Use Case này cho phép Admin quản lý toàn bộ các **chiến dịch khuyến mãi** trong hệ thống bán giày trực tuyến.  
Chiến dịch khuyến mãi là đơn vị tổ chức khuyến mãi ở mức **chiến lược**, định nghĩa các quy tắc giảm giá chung mà nhiều voucher có thể kế thừa và sử dụng.

Thông qua Use Case này, Admin có thể:
- Xem danh sách chiến dịch
- Xem chi tiết chiến dịch
- Tạo mới chiến dịch
- Chỉnh sửa chiến dịch
- Bật / tắt chiến dịch
- Xóa chiến dịch

Việc thay đổi trạng thái chiến dịch ảnh hưởng trực tiếp đến khả năng áp dụng các voucher thuộc chiến dịch đó.

## Pre-Conditions
- Admin đã đăng nhập hệ thống
- Admin có quyền quản trị

## Post-Conditions
- Chiến dịch được tạo, chỉnh sửa, bật/tắt hoặc xóa thành công và lưu trong CSDL
- Trạng thái hiệu lực của chiến dịch được cập nhật dựa trên:
  - Ngày hiện tại
  - Thời gian bắt đầu – kết thúc
  - Trạng thái bật/tắt
- Nếu chiến dịch bị tắt hoặc hết hạn:
  - Tất cả voucher thuộc chiến dịch sẽ không được áp dụng

## Main Flow

### 1. Truy cập chức năng chiến dịch
1.1 Admin chọn menu **Quản lý khuyến mãi** → tab **Chiến dịch khuyến mãi**  
1.2 Hệ thống hiển thị danh sách chiến dịch

### 2. Hiển thị danh sách chiến dịch
2.1 Mỗi chiến dịch hiển thị:
- Tên chiến dịch
- Ngày bắt đầu – kết thúc
- Trạng thái bật/tắt
- Trạng thái hiệu lực (Sắp bắt đầu / Đang hiệu lực / Đã kết thúc)

2.2 Danh sách có:
- Ô tìm kiếm theo tên
- Bộ lọc
- Nút **Thêm chiến dịch mới**

### 3. Thêm chiến dịch mới
3.1 Admin chọn **Thêm chiến dịch mới**  
3.2 Hệ thống hiển thị form gồm:
- Tên chiến dịch
- Mô tả
- Loại giảm giá (% / số tiền)
- Giá trị giảm
- Mức giảm tối đa
- Giá trị đơn hàng tối thiểu
- Thời gian bắt đầu – kết thúc
- Trạng thái bật/tắt
- Đối tượng áp dụng

3.3 Admin nhập thông tin và nhấn **Lưu**  
3.4 Hệ thống kiểm tra hợp lệ và lưu chiến dịch  
3.5 Hiển thị thông báo **“Thêm chiến dịch thành công”**

### 4. Xem chi tiết chiến dịch
4.1 Admin chọn tên chiến dịch  
4.2 Hệ thống hiển thị:
- Thông tin chi tiết chiến dịch
- Danh sách voucher thuộc chiến dịch
- Các nút: Sửa / Bật-Tắt / Xóa / Thêm voucher / Quay lại

### 5. Sửa chiến dịch
5.1 Admin chọn **Sửa chiến dịch**  
5.2 Hệ thống hiển thị form chỉnh sửa  
5.3 Kiểm tra hợp lệ và cập nhật  
5.4 Hiển thị thông báo **“Cập nhật chiến dịch thành công”**

### 6. Bật / Tắt chiến dịch
6.1 Admin chọn **Bật/Tắt**  
6.2 Hệ thống cập nhật trạng thái  
6.3 Tính lại trạng thái hiệu lực  
6.4 Cập nhật lại danh sách và chi tiết

### 7. Xóa chiến dịch
7.1 Admin chọn **Xóa**  
7.2 Hệ thống yêu cầu xác nhận  
7.3 Kiểm tra điều kiện xóa  
7.4 Thực hiện **soft delete**  
7.5 Hiển thị thông báo **“Xóa chiến dịch thành công”**

## Alternate Flow
- AF1: Tìm kiếm và lọc chiến dịch
- AF2: Tắt chiến dịch thay vì xóa
- AF3: Thêm voucher từ trang chi tiết chiến dịch

## Exception Flow
- EF1: Nhập sai định dạng dữ liệu
- EF2: Không thể xóa do voucher đã được sử dụng
- EF3: Thời gian chiến dịch không hợp lệ
- EF4: Lỗi khi tải dữ liệu chiến dịch

# 4.1.25. Manage Voucher – Quản lý voucher

## Use Case ID
UC-25

## Use Case Name
Manage Voucher

## Actor(s)
- ADMIN

## Trigger
Admin chọn tab **Voucher** hoặc thao tác từ trang chi tiết chiến dịch.

## Description
Voucher là mã khuyến mãi cụ thể để khách hàng nhập khi đặt hàng.  
Mỗi voucher:
- Thuộc về một **Campaign**
- Kế thừa quy tắc giảm giá từ Campaign
- Có thông tin riêng như mã, tiêu đề, thời gian hiệu lực, trạng thái

## Pre-Conditions
- Admin đã đăng nhập
- Có ít nhất một Campaign tồn tại

## Post-Conditions
- Voucher được tạo, cập nhật, bật/tắt hoặc xóa
- Trạng thái hiệu lực được tính đúng dựa trên:
  - Thời gian voucher
  - Trạng thái voucher
  - Trạng thái và hiệu lực chiến dịch

## Main Flow

### 1. Hiển thị danh sách voucher
- Mã voucher
- Tiêu đề
- Chiến dịch
- Thời gian hiệu lực
- Trạng thái bật/tắt
- Trạng thái hiệu lực
- Nút: Xem / Sửa / Xóa / Bật-Tắt

### 2. Thêm voucher mới
2.1 Admin chọn **Thêm voucher mới**  
2.2 Nhập thông tin:
- Mã voucher
- Tiêu đề
- Mô tả
- Thời gian hiệu lực
- Trạng thái bật/tắt
- Chiến dịch

2.3 Hệ thống kiểm tra hợp lệ  
2.4 Lưu voucher  
2.5 Hiển thị **“Thêm voucher thành công”**

### 3. Xem chi tiết voucher
- Thông tin voucher
- Thông tin chiến dịch
- Quy tắc giảm giá kế thừa
- Các nút thao tác

### 4. Sửa voucher
4.1 Admin chọn **Sửa**  
4.2 Hệ thống hiển thị form  
4.3 Kiểm tra hợp lệ  
4.4 Cập nhật voucher  
4.5 Hiển thị **“Cập nhật voucher thành công”**

### 5. Bật / Tắt voucher
5.1 Admin chọn **Bật/Tắt**  
5.2 Kiểm tra trạng thái chiến dịch  
5.3 Cập nhật trạng thái voucher

### 6. Xóa voucher
6.1 Admin chọn **Xóa**  
6.2 Kiểm tra voucher đã được sử dụng chưa  
6.3 Nếu hợp lệ → xóa  
6.4 Hiển thị **“Xóa voucher thành công”**

## Alternate Flow
- AF1: Tìm kiếm và lọc voucher
- AF2: Sửa voucher từ trang chi tiết chiến dịch
- AF3: Tự động cập nhật trạng thái khi đổi ngày

## Exception Flow
- EF1: Sai định dạng dữ liệu
- EF2: Thời gian voucher không hợp lệ
- EF3: Trùng mã voucher
- EF4: Campaign bị tắt
- EF5: Voucher vượt thời gian chiến dịch
- EF6: Voucher đã được sử dụng
- EF7: Campaign bị tắt làm voucher không hoạt động
