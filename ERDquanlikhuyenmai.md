# ERD – Promotion Campaign, Voucher & Order Voucher System

## Tổng quan
ERD mô tả cấu trúc dữ liệu cho hệ thống quản lý khuyến mãi của website bán giày, bao gồm:
- Chiến dịch khuyến mãi (Promotion Campaign)
- Voucher (mã giảm giá)
- Đối tượng áp dụng khuyến mãi (Promotion Target)
- Liên kết giữa đơn hàng và voucher (Order Voucher)

Hệ thống được thiết kế theo mô hình:
Campaign → Voucher → Order  
trong đó voucher kế thừa quy tắc giảm giá từ campaign và được áp dụng vào đơn hàng thông qua bảng liên kết.

---

## Entity: promotioncampaign

### Mô tả
Bảng promotioncampaign lưu thông tin các chiến dịch khuyến mãi ở mức chiến lược.
Mỗi chiến dịch định nghĩa các quy tắc giảm giá chung và phạm vi thời gian áp dụng cho các voucher.

### Thuộc tính
- campaignId (PK, int8): Định danh duy nhất của chiến dịch
- name (varchar): Tên chiến dịch
- description (text): Mô tả chi tiết chiến dịch
- startDate (date): Ngày bắt đầu chiến dịch
- endDate (date): Ngày kết thúc chiến dịch
- discountType (voucher_discount_type): Loại giảm giá
- discountValue (numeric): Giá trị giảm
- maxDiscountAmount (numeric): Mức giảm tối đa
- minOrderValue (numeric): Giá trị đơn hàng tối thiểu
- status (promotion_campaign_status): Trạng thái nghiệp vụ của chiến dịch
- enabled (bool): Trạng thái bật / tắt chiến dịch

---

## Entity: voucher

### Mô tả
Bảng voucher lưu thông tin các mã khuyến mãi cụ thể mà khách hàng nhập khi đặt hàng.
Mỗi voucher bắt buộc thuộc về một chiến dịch khuyến mãi và chịu sự ràng buộc bởi trạng thái và thời gian của chiến dịch đó.

### Thuộc tính
- voucherId (PK, int8): Định danh voucher
- code (varchar): Mã voucher (duy nhất)
- title (varchar): Tiêu đề voucher
- description (text): Mô tả voucher
- discountValue (numeric): Giá trị giảm
- maxDiscountValue (numeric): Mức giảm tối đa
- minOrderValue (numeric): Giá trị đơn hàng tối thiểu
- startDate (date): Ngày bắt đầu hiệu lực voucher
- endDate (date): Ngày kết thúc hiệu lực voucher
- maxRedeemPerCustomer (int8): Số lần tối đa mỗi khách hàng được sử dụng
- enabled (bool): Trạng thái bật / tắt voucher
- discountType (voucher_discount_type): Loại giảm giá
- campaignId (FK, int8): Khóa ngoại tham chiếu promotioncampaign.campaignId

---

## Entity: promotiontarget

### Mô tả
Bảng promotiontarget xác định đối tượng áp dụng khuyến mãi cho chiến dịch.
Một chiến dịch có thể áp dụng cho toàn bộ sản phẩm, theo danh mục hoặc theo sản phẩm cụ thể.

### Thuộc tính
- targetId (PK, int8): Định danh đối tượng áp dụng
- targetType (promotion_target_type): Loại đối tượng áp dụng
- shoeId (int8, nullable): ID sản phẩm, dùng khi áp dụng theo sản phẩm
- categoryId (int8, nullable): ID danh mục, dùng khi áp dụng theo danh mục
- campaignId (FK, int8): Khóa ngoại tham chiếu promotioncampaign.campaignId

---

## Entity: ordervoucher

### Mô tả
Bảng ordervoucher là bảng liên kết giữa đơn hàng và voucher,
ghi nhận việc một voucher đã được sử dụng trong một đơn hàng cụ thể.
Bảng này phục vụ cho việc:
- Theo dõi lịch sử sử dụng voucher
- Ngăn việc xóa voucher đã được dùng
- Tính số lần sử dụng voucher theo khách hàng hoặc theo hệ thống

### Thuộc tính
- orderVoucherId (PK, int8): Định danh bản ghi
- orderId (FK, int8): Khóa ngoại tham chiếu bảng Order
- voucherId (FK, int8): Khóa ngoại tham chiếu bảng voucher
- discountAmount (numeric): Số tiền giảm thực tế áp dụng cho đơn hàng
- appliedAt (timestamptz): Thời điểm áp dụng voucher

---

## Relationships

- promotioncampaign – voucher: One-to-Many (1–N)  
  Một chiến dịch có thể có nhiều voucher, mỗi voucher chỉ thuộc về một chiến dịch duy nhất.

- promotioncampaign – promotiontarget: One-to-Many (1–N)  
  Một chiến dịch có thể áp dụng cho nhiều đối tượng khuyến mãi.

- voucher – ordervoucher: One-to-Many (1–N)  
  Một voucher có thể được sử dụng trong nhiều đơn hàng khác nhau (theo thời gian).

- order – ordervoucher: One-to-Many (1–N)  
  Một đơn hàng có thể áp dụng tối đa một hoặc nhiều voucher (tùy rule hệ thống).

---

## Enum Definitions

### voucher_discount_type
- PERCENT: Giảm theo phần trăm
- FIXED_AMOUNT: Giảm theo số tiền cố định

### promotion_campaign_status
- UPCOMING: Sắp bắt đầu
- ACTIVE: Đang hiệu lực
- EXPIRED: Đã kết thúc
- DISABLED: Không hoạt động

### promotion_target_type
- ALL_PRODUCTS: Áp dụng toàn bộ sản phẩm
- CATEGORY: Áp dụng theo danh mục
- PRODUCT: Áp dụng theo sản phẩm cụ thể

---

## Business Rules

- Voucher không tồn tại độc lập, luôn phải gắn với một campaign
- Voucher chỉ được áp dụng khi:
  - Voucher enabled = true
  - Campaign enabled = true
  - Thời gian hiện tại nằm trong khoảng hiệu lực của voucher và campaign
- Khi campaign bị tắt hoặc hết hạn:
  - Tất cả voucher thuộc campaign tự động chuyển sang trạng thái không hoạt động
- Voucher đã xuất hiện trong bảng ordervoucher:
  - Không được phép xóa
  - Chỉ được tắt để ngừng áp dụng
