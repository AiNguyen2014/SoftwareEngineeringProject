# Use Case [26]: Xem và sử dụng mã giảm giá (Customer)

## Actor(s)
- **CUSTOMER** (Khách hàng đã đăng nhập)

## Trigger
- Customer áp dụng voucher khi thanh toán (tại trang checkout).

**LƯU Ý**: Code có endpoint `GET /vouchers` và template `user/voucher-list.html` để xem danh sách voucher, NHƯNG hiện chưa có link navigation từ menu chính. Trang này là tính năng phụ/optional, chưa được integrate đầy đủ vào user flow.

## Description
Customer sử dụng voucher (mã giảm giá) khi đặt hàng để được giảm giá. Customer có thể:
- Nhập mã voucher trực tiếp tại trang checkout
- (Optional) Truy cập trang `/vouchers` để xem danh sách voucher khả dụng (nếu biết URL)

Mỗi voucher hiển thị:
- Mã voucher, tiêu đề, mô tả
- Loại giảm giá (phần trăm / số tiền cố định)
- Giá trị giảm, mức giảm tối đa (nếu có)
- Điều kiện: Giá trị đơn hàng tối thiểu
- Thời gian hiệu lực
- Số lần sử dụng tối đa/khách hàng (nếu có)
- Trạng thái: Áp dụng được / Không đủ điều kiện (hiển thị lý do)

## Pre-Conditions
- Customer đã đăng nhập vào hệ thống (có `USER_ID` trong session).
- Có ít nhất một voucher được enabled và trong thời gian hiệu lực.
- Campaign của voucher phải enabled và trong thời gian hiệu lực.

## Post-Conditions
- Voucher được áp dụng vào đơn hàng (lưu vào session hoặc `order_voucher`).
- Số tiền giảm giá được tính toán và hiển thị.
- Tổng tiền thanh toán được cập nhật sau khi trừ giảm giá.

---

## Main Flow

### 1. Áp dụng voucher vào đơn hàng (FLOW CHÍNH)
1. Customer nhập **Mã voucher** vào ô input tại trang thanh toán (checkout).
2. Customer click nút **Áp dụng**.
3. Frontend gọi API: `POST /vouchers/api/validate`
   - Request body: `voucherCode`, `orderSubTotal`
   - Session: `USER_ID`
4. Hệ thống validate voucher (xem flow 1.A).
5. Nếu hợp lệ:
   - Hệ thống tính discount amount theo `discountType`:
     - **PERCENT**: `discount = (orderSubTotal * discountValue / 100)`, giới hạn bởi `maxDiscountValue`
     - **FIXED_AMOUNT**: `discount = discountValue`
     - Đảm bảo `discount <= orderSubTotal`
   - Response JSON:
     ```json
     {
       "valid": true,
       "discountAmount": 50000,
       "voucherCode": "GIAM50K",
       "voucherTitle": "Giảm 50K cho đơn từ 500K",
       "message": "Áp dụng thành công! Giảm 50,000₫"
     }
     ```
   - Lưu `APPLIED_VOUCHER_CODE` vào session
   - Frontend cập nhật UI: Hiển thị voucher đã chọn, tổng tiền sau giảm
6. Nếu không hợp lệ:
   - Response JSON với `valid: false` và `message` (lý do lỗi)
   - Frontend hiển thị thông báo lỗi

### 1.A. Validate voucher (8 bước kiểm tra)
Hệ thống kiểm tra các điều kiện sau (theo thứ tự):

1. **Voucher tồn tại**: Tìm trong DB bằng `code`
   - Nếu không tồn tại → Fail: "Mã voucher không tồn tại"
2. **Voucher enabled**: `voucher.enabled == true`
   - Nếu false → Fail: "Mã voucher không khả dụng"
3. **Campaign enabled**: `voucher.campaign.enabled == true`
   - Nếu false → Fail: "Chiến dịch của voucher đã bị tắt"
4. **Thời gian voucher**: `today ∈ [voucher.startDate, voucher.endDate]`
   - Nếu `today < startDate` → Fail: "Mã voucher chưa có hiệu lực"
   - Nếu `today > endDate` → Fail: "Mã voucher đã hết hạn"
5. **Thời gian campaign**: `today ∈ [campaign.startDate, campaign.endDate]`
   - Nếu `today < campaign.startDate` → Fail: "Chiến dịch chưa bắt đầu"
   - Nếu `today > campaign.endDate` → Fail: "Chiến dịch đã kết thúc"
6. **Giá trị đơn hàng tối thiểu**: `minOrderValue == null OR orderSubTotal >= minOrderValue`
   - Nếu không đủ → Fail: "Đơn hàng phải đạt tối thiểu {minOrderValue}₫ để áp dụng voucher này"
7. **Giới hạn lượt dùng**: `maxRedeemPerCustomer == null OR userUsageCount < maxRedeemPerCustomer`
   - Query: `orderVoucherRepository.countByVoucher_VoucherIdAndUserId(voucherId, userId)`
   - Nếu đã dùng hết → Fail: "Bạn đã sử dụng hết lượt áp dụng voucher này"
8. **Tất cả điều kiện hợp lệ** → Success: Trả về voucher và discountAmount

### 2. Xóa voucher đã chọn
1. Customer click nút **Xóa** voucher đã áp dụng.
2. Frontend gọi API: `POST /vouchers/api/remove`
3. Hệ thống xóa `APPLIED_VOUCHER_CODE` khỏi session.
4. Response JSON: `{"success": true, "message": "Đã xóa mã giảm giá"}`
5. Frontend cập nhật UI: Ẩn voucher đã chọn, cập nhật lại tổng tiền (không giảm giá).

### 4. Kiểm tra số lần đã sử dụng voucher (API)
1. Frontend gọi API: `GET /vouchers/api/usage/{voucherId}`
2. Hệ thống lấy `USER_ID` từ session.
3. Nếu có `USER_ID`:
   - Query: `orderVoucherRepository.countByVoucher_VoucherIdAndUserId(voucherId, userId)`
   - Response: `{"usageCount": 2, "loggedIn": true}`
4. Nếu không có `USER_ID` (chưa đăng nhập):
   - Response: `{"usageCount": 0, "loggedIn": false}`
5. Frontend hiển thị thông tin: "Bạn đã dùng 2/{maxRedeemPerCustomer} lượt"

### 4. Lưu voucher vào order (sau khi tạo order)
1. Sau khi Customer xác nhận đặt hàng và Order được tạo (có `orderId`).
2. Nếu có `APPLIED_VOUCHER_CODE` trong session:
   - Hệ thống gọi `customerPromotionService.applyVoucherToOrder(order, voucher, userId)`
3. Hệ thống validate lại voucher với `order.subTotal` (flow 2.A).
4. Nếu hợp lệ:
   - Tạo record trong bảng `order_voucher`:
     - `order_id`: ID của order vừa tạo
     - `voucher_id`: ID của voucher
     - `user_id`: ID của customer
     - `applied_amount`: Số tiền đã giảm
   - Log: "Applied voucher {code} to order {orderId} with discount {discountAmount}"
5. Nếu không hợp lệ:
   - Throw exception với error message từ validation
   - Order vẫn được tạo nhưng không có voucher

### 5. [OPTIONAL] Xem danh sách voucher khả dụng
**LƯU Ý**: Tính năng này đã được implement (endpoint `GET /vouchers`, template `user/voucher-list.html`) NHƯNG chưa có link navigation từ menu chính. Customer phải biết URL `/vouchers` để truy cập.

1. Customer truy cập trực tiếp URL `/vouchers` (hoặc từ link nếu có trong tương lai).
2. Hệ thố Improvements**:
- Thêm link "Mã giảm giá" vào menu navigation chính (header hoặc footer)
- Thêm breadcrumb navigation
- Integrate với checkout flow (ví dụ: "Xem mã giảm giá khả dụng" button tại checkout)

### 5.A. API lấy voucher khả dụng theo giá trị đơn hàng
**Đây là API được sử dụng trong trang checkout để hiển thị voucher phù hợp.**

1. Frontend tại trang checkout gọi API:
   - `GET /vouchers/api/available?orderSubTotal={amount}`
2. Hệ thống filter voucher:
   - T1. Customer chưa đăng nhập
- Khi customer truy cập `/vouchers` mà chưa đăng nhập:
  - Hệ thống hiển thị danh sách voucher (xem được thông tin)
  - Nhưng không thể áp dụng voucher (yêu cầu đăng nhập khi checkout)
- Khi gọi API validate mà `USER_ID == null`:
  - Response: `{"valid": false, "message": "Vui lòng đăng nhập để sử dụng mã giảm giá"}`

### AF2. Không có voucher khả dụng (tại trang /vouchers)
- Khi không có voucher nào thỏa điều kiện (enabled, thời gian, campaign):
  - Hệ thống trả về danh sách rỗng
  - UI hiển thị empty state:
    - Icon voucher lớn
    - Tiêu đề: "Chưa có mã giảm giá nào"
    - Mô tả: "Vui lòng quay lại sau để nhận ưu đãi tốt nhất!"
    - Nút "Quay lại trang chủ"
4. UI tại checkout hiển thị danh sách voucher có thể chọn.

---

## Alternate Flow

### AF1. Không có voucher khả dụng
- Khi không có voucher nào thỏa điều kiện (enabled, thời gian, campaign):
  - Hệ thống trả về danh sách rỗng
  - UI hiển thị thông báo: "Hiện chưa có mã giảm giá nào"

### AF2. Voucher không đủ điều kiện áp dụng
- Khi voucher hiển thị trong danh sách nhưng chưa đủ điều kiện:
  - UI hiển thị voucher màu xám (disabled)
  - Hiển thị lý do: "Đơn hàng tối thiểu 500,000đ" hoặc "Đã dùng 5/5 lượt"
  - Không thể click nút "Sao chép mã" hoặc "Áp dụng"

### AF3. Customer chưa đăng nhập
- Khi customer truy cập `/vouchers` mà chưa đăng nhập:
  - Hệ thống hiển thị danh sách voucher (xem được thông tin)
  - Nhưng không thể áp dụng voucher (yêu cầu đăng nhập)
- Khi gọi API validate mà `USER_ID == null`:
  - Response: `{"valid": false, "message": "Vui lòng đăng nhập để sử dụng mã giảm giá"}`

---

## E3. Kiểm tra số lần đã sử dụng voucher (API - Optional

### EF1. Mã voucher không tồn tại
- Khi customer nhập mã voucher không có trong DB:
  - Hệ thống throw validation fail
  - Response: `{"valid": false, "message": "Mã voucher không tồn tại"}`
  - Frontend hiển thị error message màu đỏ

### EF2. Voucher đã hết hạn hoặc chưa có hiệu lực
- Khi `today < voucher.startDate`:
  - Response: `{"valid": false, "message": "Mã voucher chưa có hiệu lực"}`
- Khi `today > voucher.endDate`:
  - Response: `{"valid": false, "message": "Mã voucher đã hết hạn"}`

### EF3. Voucher hoặc Campaign đã bị tắt
- Khi `voucher.enabled == false`:
  - Response: `{"valid": false, "message": "Mã voucher không khả dụng"}`
- Khi `campaign.enabled == false`:
  - Response: `{"valid": false, "message": "Chiến dịch của voucher đã bị tắt"}`

### EF4. Đơn hàng chưa đủ giá trị tối thiểu
- Khi `orderSubTotal < voucher.minOrderValue`:
  - Response: `{"valid": false, "message": "Đơn hàng phải đạt tối thiểu {minOrderValue}₫ để áp dụng voucher này"}`
  - Frontend có thể hiển thị thêm: "Còn thiếu {gap}₫ nữa để áp dụng voucher"

### EF5. Đã dùng hết lượt áp dụng voucher
- Khi `userUsageCount >= voucher.maxRedeemPerCustomer`:
  - Query count từ `order_voucher` table
  - Response: `{"valid": false, "message": "Bạn đã sử dụng hết lượt áp dụng voucher này"}`

### EF6. Chiến dịch chưa bắt đầu hoặc đã kết thúc
- Khi `today < campaign.startDate`:
  - Response: `{"valid": false, "message": "Chiến dịch chưa bắt đầu"}`
- Khi `today > campaign.endDate`:
  - Response: `{"valid": false, "message": "Chiến dịch đã kết thúc"}`

### EF7. Lỗi hệ thống khi validate
- Khi có exception trong quá trình validate (DB error, null pointer, etc.):
  - Hệ thống catch exception và log error
  - Response: `{"valid": false, "message": "Lỗi hệ thống: {exception.message}"}`
  - Frontend hiển thị thông báo lỗi chung

### EF8. Voucher không còn hợp lệ khi apply vào order
- Khi validate lại voucher sau khi tạo order mà không còn hợp lệ:
  - Hệ thống throw `IllegalArgumentException` với error message từ validation
  - Order vẫn được tạo thành công nhưng không có discount
  - Customer được thông báo lỗi và phải thanh toán đủ giá gốc

---

## Technical Notes

### Discount Calculation Logic
```
IF discountType == PERCENT:
    discount = (orderSubTotal × discountValue / 100)
    IF maxDiscountValue != null AND discount > maxDiscountValue:
        discount = maxDiscountValue
ELSE IF discountType == FIXED_AMOUNT:
    discount = discountValue

IF discount > orderSubTotal:
    discount = orderSubTotal  // Không giảm quá giá trị đơn hàng
```

### Filter Priority (cho danh sách hiển thị)
1. Voucher áp dụng được (`applicable = true`) hiển thị trước
2. Sắp xếp theo giá trị giảm (`discountValue`) giảm dần
3. Voucher không áp dụng được hiển thị sau với màu xám

### Session Management
- `APPLIED_VOUCHER_CODE`: Lưu mã voucher đã chọn trong quá trình checkout
- Xóa sau khi order thành công hoặc customer click "Xóa voucher"
- Timeout theo session timeout của ứng dụng

### Database Query Optimization
- Eager fetch `campaign` khi query voucher: `findAllWithCampaign()`
- Count usage: Index trên `(voucher_id, user_id)` trong `order_voucher` table
- Filter trên application layer (Java Stream) thay vì query phức tạp

### API Response Format
Tất cả API trả về JSON với format:
```json
{
  "valid": true/false,
  "message": "Success/Error message",
  "data": { /* additional data */ }
}
```

### Validation Order
Validate theo thứ tự từ nhanh → chậm, từ cơ bản → phức tạp:
1. Check exists (memory/cache)
2. Check boolean flags (enabled)
3. Check dates (simple comparison)
4. Check amounts (calculation)
5. Query database (count usage) - cuối cùng
