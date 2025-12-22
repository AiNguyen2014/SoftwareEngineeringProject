# USE CASE DIAGRAM – SHOES SELLING WEBSITE

---

## 1. ACTORS

### Unregistered Customer
Người dùng chưa đăng ký tài khoản trên hệ thống.

### Registered Customer
Người dùng đã đăng ký và đăng nhập vào hệ thống.

### Admin
Quản trị viên hệ thống, chịu trách nhiệm quản lý sản phẩm, đơn hàng và báo cáo.

### Payment Gateway
Hệ thống bên thứ ba xử lý thanh toán trực tuyến.

---

## 2. USE CASES FOR UNREGISTERED CUSTOMER

- **Register**
  - Cho phép người dùng đăng ký tài khoản mới.
  - *Include*: Verify Email

- **Verify Email**
  - Xác thực email trong quá trình đăng ký tài khoản.

- **Search Products**
  - Tìm kiếm sản phẩm theo từ khóa.

- **Sort Products**
  - Sắp xếp danh sách sản phẩm theo tiêu chí (giá, tên, v.v.).

- **Filter Products**
  - Lọc sản phẩm theo điều kiện (loại, giá, giới tính, v.v.).

- **View Product List**
  - Xem danh sách sản phẩm.

- **View Product Details**
  - Xem thông tin chi tiết của một sản phẩm.
  - *Extend*: View Product List

---

## 3. USE CASES FOR REGISTERED CUSTOMER

(Registered Customer kế thừa toàn bộ chức năng của Unregistered Customer)

- **Login**
  - Đăng nhập vào hệ thống.
  - *Extend*: Forgot Password

- **Forgot Password**
  - Khôi phục mật khẩu khi quên.

- **Logout**
  - Đăng xuất khỏi hệ thống.

- **Manage Cart**
  - Thêm, cập nhật, xóa sản phẩm trong giỏ hàng.

- **Create Order**
  - Tạo đơn hàng từ giỏ hàng.
  - *Extend*: Apply Voucher
  - *Extend*: Payment

- **Apply Voucher**
  - Áp dụng mã giảm giá cho đơn hàng.

- **Payment**
  - Thực hiện thanh toán đơn hàng.
  - Tương tác với **Payment Gateway**
  - *Include*: Get Detail Invoice

- **Get Detail Invoice**
  - Lấy thông tin hóa đơn chi tiết sau khi thanh toán.

- **View Order History**
  - Xem lịch sử các đơn hàng đã mua.
  - *Extend*: Review Shoes

- **Review Shoes**
  - Đánh giá, nhận xét sản phẩm đã mua.

- **Manage User Profile**
  - Quản lý thông tin cá nhân của người dùng.

---

## 4. USE CASES FOR ADMIN

- **Login**
  - Đăng nhập hệ thống quản trị.

- **Logout**
  - Đăng xuất khỏi hệ thống.

- **Manage Product**
  - Quản lý thông tin sản phẩm (thêm, sửa, xóa).

- **Manage Inventory**
  - Quản lý tồn kho sản phẩm.

- **Manage Order**
  - Quản lý đơn hàng của khách hàng.

- **Manage Voucher**
  - Quản lý mã giảm giá.

- **Manage Campaign**
  - Quản lý các chương trình khuyến mãi.

- **View Sale Report**
  - Xem báo cáo bán hàng.

- **Revenue Statistics Report**
  - Xem báo cáo thống kê doanh thu.

---

## 5. ACTOR – USE CASE RELATIONSHIPS SUMMARY

- **Unregistered Customer**
  - Register, Search Products, Sort Products, Filter Products
  - View Product List, View Product Details

- **Registered Customer**
  - Kế thừa toàn bộ chức năng của Unregistered Customer
  - Login, Logout, Manage Cart, Create Order
  - Apply Voucher, Payment, View Order History
  - Review Shoes, Manage User Profile

- **Admin**
  - Login, Logout
  - Manage Product, Inventory, Order
  - Manage Voucher, Campaign
  - View Sale Report, Revenue Statistics Report

- **Payment Gateway**
  - Tham gia use case Payment

---

## 6. INCLUDE & EXTEND RELATIONSHIPS

- **Register** <<include>> Verify Email
- **Login** <<extend>> Forgot Password
- **View Product Details** <<extend>> View Product List
- **Create Order** <<extend>> Apply Voucher
- **Create Order** <<extend>> Payment
- **Payment** <<include>> Get Detail Invoice
- **View Order History** <<extend>> Review Shoes
