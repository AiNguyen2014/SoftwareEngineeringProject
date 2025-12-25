# Use Case [24]: Quản lý chiến dịch khuyến mãi (Admin)

## Actor(s)
- **ADMIN**

## Trigger
- Admin truy cập mục **Quản lý khuyến mãi** → tab **Chiến dịch khuyến mãi** trên trang quản trị.

## Description
Admin quản lý danh sách chiến dịch khuyến mãi: xem/tìm kiếm/lọc, xem chi tiết, tạo mới, chỉnh sửa, bật/tắt trạng thái và xóa chiến dịch. Chiến dịch khuyến mãi định nghĩa các quy tắc giảm giá chung mà voucher có thể kế thừa.

Thông tin chiến dịch có thể cập nhật:
- Tên chiến dịch, mô tả
- Thời gian bắt đầu – kết thúc
- Loại giảm giá (Phần trăm / Số tiền), Giá trị giảm
- Mức giảm tối đa, Giá trị đơn hàng tối thiểu
- Trạng thái bật/tắt (Enabled)
- Đối tượng áp dụng (Tất cả sản phẩm / Sản phẩm cụ thể / Danh mục cụ thể)

Trạng thái hiệu lực (Status) được tự động tính toán dựa trên:
- Trạng thái bật/tắt
- Ngày hiện tại so với thời gian bắt đầu – kết thúc

## Pre-Conditions
- Admin đã đăng nhập và có quyền quản trị khuyến mãi.

## Post-Conditions
- Thông tin chiến dịch, đối tượng áp dụng hoặc trạng thái được lưu vào cơ sở dữ liệu.
- Trạng thái hiệu lực của chiến dịch được cập nhật tự động.
- Danh sách chiến dịch phản ánh dữ liệu mới sau thao tác.

---

## Main Flow

### 1. Xem danh sách chiến dịch
1. Actor mở trang **Quản lý khuyến mãi** → tab **Chiến dịch**.
2. Hệ thống hiển thị danh sách chiến dịch.
3. Mỗi dòng hiển thị: Tên, Thời gian, Loại giảm giá, Trạng thái bật/tắt (toggle switch), Trạng thái hiệu lực.
4. Mỗi dòng có các nút: **Xem**, **Sửa**, **Xóa**.

### 1.A. Tìm kiếm & Lọc
1. Admin nhập từ khóa (tên chiến dịch) và tùy chọn chọn bộ lọc: Loại giảm giá, Trạng thái hiệu lực (DRAFT/ACTIVE/ENDED/CANCELLED), Trạng thái bật/tắt.
2. Admin nhấn **Tìm kiếm**.
3. Hệ thống lọc danh sách chiến dịch trên client-side và trả về kết quả thỏa điều kiện; có thể rỗng nếu không khớp.

### 1.B. Xem chi tiết (read-only)
1. Admin chọn nút **Xem** hoặc click tên chiến dịch.
2. Hệ thống truy vấn chiến dịch với targets và vouchers (fetch eager).
3. Hệ thống hiển thị chi tiết: thông tin chung, quy tắc giảm giá, thời gian, trạng thái, đối tượng áp dụng (products/categories), danh sách voucher thuộc chiến dịch.

### 2. Thêm chiến dịch mới
1. Admin chọn **Thêm chiến dịch mới**.
2. Hệ thống hiển thị form với các trường:
   - Tên chiến dịch* (bắt buộc)
   - Mô tả
   - Ngày bắt đầu* (bắt buộc)
   - Ngày kết thúc* (bắt buộc)
   - Loại giảm giá* (PERCENTAGE/FIXED_AMOUNT)
   - Giá trị giảm* (> 0)
   - Mức giảm tối đa
   - Giá trị đơn hàng tối thiểu
   - Trạng thái bật/tắt (mặc định: Bật)
   - Đối tượng áp dụng* (ALL/PRODUCT/CATEGORY)
3. Admin nhập thông tin và chọn đối tượng áp dụng:
   - **Tất cả sản phẩm (ALL)**: Không cần chọn gì thêm
   - **Sản phẩm cụ thể (PRODUCT)**: Chọn từ danh sách sản phẩm có sẵn (có thể chọn nhiều)
   - **Danh mục cụ thể (CATEGORY)**: Chọn từ danh sách danh mục có sẵn (có thể chọn nhiều)
4. Admin nhấn **Lưu**.
5. Hệ thống kiểm tra validation:
   - Trường bắt buộc không để trống
   - Giá trị giảm > 0
   - Ngày kết thúc >= ngày bắt đầu
6. Nếu hợp lệ:
   - Hệ thống lưu chiến dịch với enabled = true
   - Tính toán trạng thái hiệu lực (Status) dựa vào enabled và ngày hiện tại:
     - Nếu enabled = false → CANCELLED
     - Nếu ngày hiện tại < startDate → DRAFT
     - Nếu ngày hiện tại > endDate → ENDED
     - Ngược lại → ACTIVE
   - Xóa tất cả targets cũ (nếu có)
   - Lưu đối tượng áp dụng mới vào bảng `promotion_target`:
     - Nếu ALL: Tạo 1 record với targetType = ALL
     - Nếu PRODUCT: Tạo nhiều records với targetType = PRODUCT, shoe_id tương ứng
     - Nếu CATEGORY: Tạo nhiều records với targetType = CATEGORY, category_id tương ứng
7. Hệ thống hiển thị thông báo "Lưu chiến dịch thành công" và redirect về danh sách.

### 3. Chỉnh sửa chiến dịch
1. Admin chọn nút **Sửa** tại danh sách hoặc trang chi tiết.
2. Hệ thống load chiến dịch với targets hiện tại và hiển thị form chỉnh sửa, điền sẵn dữ liệu cũ.
3. Admin cập nhật các trường: Tên, Mô tả, Thời gian, Loại giảm giá, Giá trị giảm, Mức giảm tối đa, Giá trị đơn hàng tối thiểu, Đối tượng áp dụng.
4. Admin nhấn **Lưu**.
5. Hệ thống kiểm tra validation như bước 2.5.
6. Nếu hợp lệ:
   - Xóa tất cả targets cũ từ bảng `promotion_target`
   - Lưu đối tượng áp dụng mới (như bước 2.6)
   - Tính toán lại trạng thái hiệu lực
   - Cập nhật thông tin chiến dịch
7. Hệ thống hiển thị thông báo "Lưu chiến dịch thành công" và redirect về danh sách.

### 4. Bật/Tắt trạng thái chiến dịch
1. Tại danh sách, Admin click toggle switch **Bật/Tắt** của chiến dịch.
2. Form submit tự động (onchange event).
3. Hệ thống đảo trạng thái enabled: true → false hoặc false → true.
4. Hệ thống tính toán lại trạng thái hiệu lực (Status):
   - Nếu enabled = false → Status = CANCELLED
   - Nếu enabled = true → Status được tính toán dựa trên ngày hiện tại (như bước 2.6)
5. Hệ thống lưu và hiển thị thông báo "Đã cập nhật trạng thái chiến dịch".
6. Trang reload, danh sách được refresh.

### 5. Xóa chiến dịch
1. Admin chọn nút **Xóa** tại danh sách hoặc trang chi tiết.
2. Form submit request POST đến `/admin/promotions/campaigns/{id}/delete`.
3. Hệ thống kiểm tra: chiến dịch có voucher liên kết không (query `voucherRepository.existsByCampaign_CampaignId`).
4. Nếu có voucher:
   - Hệ thống throw exception: "Chiến dịch có voucher, không thể xóa"
   - Hiển thị error message
   - Không thực hiện xóa
5. Nếu không có voucher:
   - Hệ thống xóa chiến dịch (hard delete) cùng với tất cả targets (cascade)
   - Hiển thị thông báo "Đã xóa chiến dịch thành công"
6. Redirect về danh sách.

---

## Alternate Flow

### AF1. Không có kết quả tìm kiếm
- Khi bộ lọc/từ khóa không khớp chiến dịch nào, hệ thống trả về danh sách rỗng.
- Admin có thể điều chỉnh điều kiện và tìm lại.

### AF2. Chiến dịch không tồn tại khi xem/sửa/xóa/đổi trạng thái
- Hệ thống throw exception "Không tìm thấy chiến dịch".
- Hiển thị error message và redirect về danh sách.

---

## Exception Flow

### EF1. Dữ liệu không hợp lệ khi thêm/chỉnh sửa
- Thiếu trường bắt buộc, giá trị giảm ≤ 0, ngày kết thúc < ngày bắt đầu.
- Binding validation errors được detect.
- Hệ thống giữ nguyên form, hiển thị lỗi validation và yêu cầu nhập lại.

### EF2. Không thể xóa do có voucher
- Khi chiến dịch có voucher liên kết (`voucherRepository.existsByCampaign_CampaignId(id) == true`).
- Hệ thống throw `IllegalStateException`: "Chiến dịch có voucher, không thể xóa".
- Hiển thị error message, không thực hiện xóa.

### EF3. Lỗi lưu dữ liệu
- Khi có exception trong quá trình save (database error, constraint violation, etc.).
- Hệ thống log error và hiển thị thông báo lỗi chung.
- Không lưu thay đổi, giữ nguyên form để admin thử lại.

---

# Use Case [25]: Quản lý voucher (Admin)

## Actor(s)
- **ADMIN**

## Trigger
- Admin truy cập mục **Quản lý khuyến mãi** → tab **Voucher** trên trang quản trị.

## Description
Admin quản lý danh sách voucher: xem/tìm kiếm/lọc, xem chi tiết, tạo mới, chỉnh sửa, bật/tắt trạng thái và xóa voucher.

Voucher là mã khuyến mãi cụ thể để khách hàng nhập khi đặt hàng. Mỗi voucher:
- Thuộc về một chiến dịch (Campaign)
- Có các thông tin riêng: Mã, Tiêu đề, Mô tả, Thời gian hiệu lực, Số lần sử dụng tối đa/khách hàng
- Có quy tắc giảm giá riêng (discount type, value, max, min) độc lập với campaign

## Pre-Conditions
- Admin đã đăng nhập và có quyền quản trị khuyến mãi.
- Có ít nhất một Campaign tồn tại trong hệ thống.

## Post-Conditions
- Thông tin voucher hoặc trạng thái được lưu vào cơ sở dữ liệu.
- Danh sách voucher phản ánh dữ liệu mới sau thao tác.

---

## Main Flow

### 1. Xem danh sách voucher
1. Actor mở trang **Quản lý khuyến mãi** → tab **Voucher**.
2. Hệ thống hiển thị danh sách voucher (fetch với campaign eager).
3. Mỗi dòng hiển thị: Mã voucher, Tiêu đề, Chiến dịch, Thời gian hiệu lực, Trạng thái bật/tắt (toggle switch).
4. Mỗi dòng có các nút: **Xem**, **Sửa**, **Xóa**.

### 1.A. Tìm kiếm & Lọc
1. Admin nhập từ khóa (mã hoặc tiêu đề) và tùy chọn chọn bộ lọc: Chiến dịch, Loại giảm giá, Trạng thái bật/tắt.
2. Admin nhấn **Tìm kiếm**.
3. Hệ thống lọc danh sách voucher trên client-side và trả về kết quả thỏa điều kiện; có thể rỗng nếu không khớp.

### 1.B. Xem chi tiết (read-only)
1. Admin chọn nút **Xem** hoặc click mã voucher.
2. Hệ thống truy vấn voucher với campaign (fetch eager).
3. Hệ thống hiển thị chi tiết: mã, tiêu đề, mô tả, chiến dịch liên kết, quy tắc giảm giá, thời gian, số lần sử dụng tối đa, trạng thái.

### 2. Thêm voucher mới
1. Admin chọn **Thêm voucher mới**.
2. Hệ thống hiển thị form với các trường:
   - Mã voucher* (bắt buộc, unique)
   - Tiêu đề
   - Mô tả
   - Chiến dịch* (dropdown, bắt buộc)
   - Loại giảm giá* (PERCENTAGE/FIXED_AMOUNT)
   - Giá trị giảm* (> 0)
   - Mức giảm tối đa
   - Giá trị đơn hàng tối thiểu
   - Ngày bắt đầu* (bắt buộc)
   - Ngày kết thúc* (bắt buộc)
   - Số lần sử dụng tối đa/khách hàng
   - Trạng thái bật/tắt (mặc định: Bật)
3. Admin nhập thông tin và nhấn **Lưu**.
4. Hệ thống kiểm tra validation:
   - Trường bắt buộc không để trống
   - Mã voucher chưa tồn tại (unique check: `voucherRepository.existsByCode`)
   - Giá trị giảm > 0
   - Ngày kết thúc >= ngày bắt đầu
   - Campaign tồn tại
5. Nếu hợp lệ:
   - Trim code và title
   - Lưu voucher với enabled = true, liên kết với campaign
6. Hệ thống hiển thị thông báo "Lưu voucher thành công" và redirect về danh sách.

### 3. Chỉnh sửa voucher
1. Admin chọn nút **Sửa** tại danh sách hoặc trang chi tiết.
2. Hệ thống load voucher với campaign và hiển thị form chỉnh sửa, điền sẵn dữ liệu cũ.
3. Admin cập nhật các trường (trừ mã voucher - không cho sửa).
4. Admin nhấn **Lưu**.
5. Hệ thống kiểm tra validation như bước 2.4 (trừ unique check cho code).
6. Nếu hợp lệ:
   - Trim code và title
   - Cập nhật thông tin voucher
7. Hệ thống hiển thị thông báo "Lưu voucher thành công" và redirect về danh sách.

### 4. Bật/Tắt trạng thái voucher
1. Tại danh sách, Admin click toggle switch **Bật/Tắt** của voucher.
2. Form submit tự động (onchange event).
3. Hệ thống đảo trạng thái enabled: true → false hoặc false → true.
4. Hệ thống lưu và hiển thị thông báo "Đã cập nhật trạng thái voucher".
5. Trang reload, danh sách được refresh.

### 5. Xóa voucher
1. Admin chọn nút **Xóa** tại danh sách hoặc trang chi tiết.
2. Form submit request POST đến `/admin/promotions/vouchers/{id}/delete`.
3. Hệ thống kiểm tra: voucher đã được sử dụng trong order chưa.
   - **Lưu ý**: Hiện tại code có comment TODO, chưa implement check này.
   - Khi module Order được implement, sẽ check: `orderVoucherRepository.existsByVoucher_VoucherId(id)`
4. Nếu đã được sử dụng (khi implement):
   - Hệ thống throw exception: "Voucher đã được sử dụng, không thể xóa"
   - Hiển thị error message
   - Không thực hiện xóa
5. Nếu chưa được sử dụng:
   - Hệ thống xóa voucher (hard delete)
   - Hiển thị thông báo "Đã xóa voucher"
6. Redirect về danh sách.

---

## Alternate Flow

### AF1. Không có kết quả tìm kiếm
- Khi bộ lọc/từ khóa không khớp voucher nào, hệ thống trả về danh sách rỗng.
- Admin có thể điều chỉnh điều kiện và tìm lại.

### AF2. Voucher không tồn tại khi xem/sửa/xóa/đổi trạng thái
- Hệ thống throw exception "Không tìm thấy voucher".
- Hiển thị error message và redirect về danh sách.

---

## Exception Flow

### EF1. Dữ liệu không hợp lệ khi thêm/chỉnh sửa
- Thiếu trường bắt buộc, giá trị giảm ≤ 0, ngày kết thúc < ngày bắt đầu.
- Binding validation errors được detect.
- Hệ thống giữ nguyên form, hiển thị lỗi validation và yêu cầu nhập lại.

### EF2. Mã voucher đã tồn tại (khi tạo mới)
- Khi create voucher mới mà code đã tồn tại.
- Hệ thống throw exception: "Mã voucher đã tồn tại".
- Hiển thị error message, giữ nguyên form để admin nhập lại.

### EF3. Chiến dịch không tồn tại
- Khi campaign được chọn không tồn tại trong DB.
- Hệ thống throw exception: "Không tìm thấy chiến dịch".
- Hiển thị error message, giữ nguyên form.

### EF4. Không thể xóa do voucher đã được sử dụng (khi implement)
- Khi voucher đã được sử dụng trong order (`orderVoucherRepository.existsByVoucher_VoucherId(id) == true`).
- Hệ thống throw exception: "Voucher đã được sử dụng, không thể xóa".
- Hiển thị error message, không thực hiện xóa.

### EF5. Lỗi lưu dữ liệu
- Khi có exception trong quá trình save (database error, constraint violation, etc.).
- Hệ thống log error và hiển thị thông báo lỗi chung.
- Không lưu thay đổi, giữ nguyên form để admin thử lại.
