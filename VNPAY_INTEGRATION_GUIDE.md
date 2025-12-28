# Hướng dẫn tích hợp VNPay Payment Gateway

## Tổng quan
Hệ thống đã được tích hợp VNPay Payment Gateway để hỗ trợ thanh toán trực tuyến cho khách hàng.

## Thông tin tài khoản VNPay Sandbox (Test)

### Cấu hình VNPay
- **Terminal ID (vnp_TmnCode)**: YZ312VU8
- **Secret Key (vnp_HashSecret)**: X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6
- **URL thanh toán**: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
- **Merchant Admin**: https://sandbox.vnpayment.vn/merchantv2/
- **Tên đăng nhập**: lehoa240909@gmail.com

### Thẻ test
- **Ngân hàng**: NCB
- **Số thẻ**: 9704198526191432198
- **Tên chủ thẻ**: NGUYEN VAN A
- **Ngày phát hành**: 07/15
- **Mật khẩu OTP**: 123456

## Cài đặt và cấu hình

### 1. Chạy migration database
```sql
-- Chạy file migration_vnpay_payment.sql trong database
psql -U postgres -d your_database -f migration_vnpay_payment.sql
```

Hoặc thực hiện thủ công các bước:
1. Tạo bảng `payment`
2. Thêm cột `paymentStatus` vào bảng `order`
3. Tạo các indexes cần thiết

### 2. Cấu hình trong application.properties
File `application.properties` đã được cập nhật với cấu hình VNPay:
```properties
vnpay.tmn-code=YZ312VU8
vnpay.hash-secret=X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:8080/payment/vnpay-return
vnpay.api-url=https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
```

**Lưu ý**: Khi deploy lên production, cần cập nhật:
- `vnpay.return-url` với domain thực tế
- Thay đổi từ sandbox sang môi trường production

### 3. Build và chạy ứng dụng
```bash
./mvnw clean compile
./mvnw spring-boot:run
```

## Flow thanh toán VNPay

### 1. User Flow
1. Khách hàng chọn sản phẩm và thêm vào giỏ hàng
2. Vào trang checkout, nhập thông tin giao hàng
3. Chọn phương thức thanh toán "Thanh toán qua VNPay"
4. Nhấn "Xác nhận đặt hàng"
5. Hệ thống tạo đơn hàng với status "PENDING" và paymentStatus "UNPAID"
6. Redirect sang VNPay để thực hiện thanh toán
7. Khách hàng nhập thông tin thẻ và xác thực OTP
8. VNPay xử lý và redirect về hệ thống với kết quả
9. Hệ thống cập nhật trạng thái và hiển thị kết quả

### 2. Technical Flow

#### A. Tạo Payment URL
```java
// OrderController.createOrder()
- Tạo Order với paymentMethod = "VNPAY"
- Redirect to /payment/create-vnpay?orderId={orderId}

// PaymentController.createVNPayPayment()
- Tạo Payment record với status = "PENDING"
- Gọi VNPayService.createPaymentUrl()
- Redirect user đến VNPay
```

#### B. VNPay Callback
```java
// PaymentController.vnpayReturn()
- Nhận params từ VNPay
- Verify signature (HMAC SHA512)
- Kiểm tra vnp_ResponseCode
  + "00": Thành công → Update Payment & Order status → Redirect /payment/success
  + Khác: Thất bại → Update Payment status → Redirect /payment/failed
```

## Cấu trúc Code

### Entities
1. **Payment.java** - Entity lưu thông tin giao dịch
   - paymentId, orderId, userId
   - paymentMethod, amount, status
   - vnpTransactionNo, vnpResponseCode, vnpBankCode, etc.

2. **Order.java** - Đã thêm field `paymentStatus`
   - "UNPAID": Chưa thanh toán
   - "PAID": Đã thanh toán
   - "REFUNDED": Đã hoàn tiền

### Services
1. **VNPayService.java**
   - `createPaymentUrl()`: Tạo URL thanh toán VNPay
   - `verifyPaymentCallback()`: Verify chữ ký từ VNPay
   - `hmacSHA512()`: Hash HMAC SHA512
   - `getResponseMessage()`: Parse response code

2. **OrderService.java**
   - Đã cập nhật để set paymentStatus khi tạo order

### Controllers
1. **PaymentController.java**
   - `POST /payment/create-vnpay`: Tạo payment và redirect VNPay
   - `GET /payment/vnpay-return`: Nhận callback từ VNPay
   - `GET /payment/success`: Trang thanh toán thành công
   - `GET /payment/failed`: Trang thanh toán thất bại
   - `GET /payment/status/{orderId}`: API check payment status

2. **OrderController.java**
   - Đã cập nhật `/order/create` để xử lý VNPAY method

### Templates
1. **payment.html** - Chọn phương thức thanh toán (COD/VNPay)
2. **payment-success.html** - Hiển thị kết quả thanh toán thành công
3. **payment-failed.html** - Hiển thị kết quả thanh toán thất bại

## Testing

### 1. Test thanh toán thành công
1. Truy cập: http://localhost:8080
2. Thêm sản phẩm vào giỏ
3. Checkout → Chọn VNPay
4. Nhập thông tin thẻ test (xem phần Thẻ test ở trên)
5. Nhập OTP: 123456
6. Kiểm tra redirect về /payment/success
7. Kiểm tra database:
   - Order: paymentStatus = "PAID", status = "PENDING"
   - Payment: status = "SUCCESS"

### 2. Test thanh toán thất bại
1. Làm tương tự nhưng hủy giao dịch trên VNPay
2. Hoặc để hết timeout (15 phút)
3. Kiểm tra redirect về /payment/failed
4. Kiểm tra database:
   - Order: paymentStatus = "UNPAID"
   - Payment: status = "FAILED"

### 3. Test COD (không thay đổi)
1. Chọn COD khi checkout
2. Order được tạo với paymentStatus = "UNPAID"
3. Không có Payment record

## Response Codes từ VNPay

| Code | Ý nghĩa |
|------|---------|
| 00 | Giao dịch thành công |
| 07 | Trừ tiền thành công nhưng giao dịch nghi ngờ |
| 09 | Thẻ chưa đăng ký Internet Banking |
| 10 | Xác thực sai quá 3 lần |
| 11 | Hết hạn chờ thanh toán |
| 12 | Thẻ bị khóa |
| 13 | Nhập sai OTP |
| 24 | Khách hàng hủy giao dịch |
| 51 | Tài khoản không đủ số dư |
| 65 | Vượt quá hạn mức giao dịch |
| 75 | Ngân hàng đang bảo trì |

## Security

### 1. Signature Verification
- Tất cả callback từ VNPay đều được verify chữ ký HMAC SHA512
- Sử dụng Secret Key để tạo và verify signature
- Nếu signature không khớp → từ chối request

### 2. Data Validation
- Kiểm tra orderId thuộc về user đang login
- Kiểm tra order status trước khi cho phép thanh toán
- Validate amount, transaction reference

### 3. Timeout
- VNPay payment URL hết hạn sau 15 phút
- User phải hoàn thành thanh toán trong thời gian này

## Troubleshooting

### 1. Lỗi "Chữ ký không hợp lệ"
- Kiểm tra vnp_HashSecret trong application.properties
- Đảm bảo không có space/newline trong config
- Kiểm tra encoding khi build hash data

### 2. Lỗi "Không thể kết nối VNPay"
- Kiểm tra internet connection
- Verify vnp_Url đúng (sandbox hoặc production)
- Check firewall/proxy settings

### 3. Callback không nhận được
- Kiểm tra vnp_ReturnUrl accessible từ internet (nếu deploy)
- Với localhost, cần dùng ngrok hoặc công cụ tương tự
- Kiểm tra routing và controller mapping

### 4. Database errors
- Đảm bảo migration đã chạy thành công
- Kiểm tra foreign keys tồn tại
- Verify column types khớp với entity

## Production Deployment

### 1. Cập nhật cấu hình
```properties
# Thay đổi sang production
vnpay.url=https://vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=https://yourdomain.com/payment/vnpay-return
vnpay.api-url=https://vnpayment.vn/merchant_webapi/api/transaction

# Cập nhật TMN Code và Secret Key từ VNPay production
vnpay.tmn-code=YOUR_PRODUCTION_TMN_CODE
vnpay.hash-secret=YOUR_PRODUCTION_SECRET_KEY
```

### 2. Đăng ký IPN URL với VNPay
- Cung cấp IPN URL (server-to-server callback) cho VNPay
- URL này dùng để VNPay gọi lại khi có thay đổi trạng thái
- Format: https://yourdomain.com/payment/vnpay-ipn

### 3. SSL Certificate
- Đảm bảo website có SSL certificate (HTTPS)
- VNPay yêu cầu HTTPS cho return URL và IPN URL

## Tài liệu tham khảo
- Tài liệu VNPay: https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
- Demo code: https://sandbox.vnpayment.vn/apis/vnpay-demo/
- Hotline hỗ trợ: 1900 55 55 77
- Email: support.vnpayment@vnpay.vn

## Changelog

### Version 1.0.0 (2024-12-25)
- ✅ Tích hợp VNPay Payment Gateway
- ✅ Tạo Payment entity và repository
- ✅ Thêm paymentStatus vào Order
- ✅ Xây dựng VNPayService với signature verification
- ✅ Tạo PaymentController xử lý callback
- ✅ Cập nhật OrderService và OrderController
- ✅ Thiết kế UI cho payment flow
- ✅ Tạo payment success/failed pages
- ✅ Viết migration script
- ✅ Viết documentation

## Support
Nếu có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ team development.
