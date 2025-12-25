# Use Case [23]: Quản lý sản phẩm (Admin)

## Actor(s)
- **ADMIN**

## Trigger
- Admin truy cập mục **Quản lý sản phẩm** trên trang quản trị.

## Description
Admin quản lý danh sách sản phẩm giày: xem/lọc/tìm kiếm theo phân trang, xem chi tiết, tạo mới, chỉnh sửa và bật/tắt trạng thái bán. Không hỗ trợ xóa sản phẩm (hard delete).

Thông tin sản phẩm có thể cập nhật:
- Tên sản phẩm, thương hiệu, loại giày (Nam/Nữ/Unisex)
- Giá cơ bản (basePrice), mô tả, bộ sưu tập (collection)
- Danh mục (category)
- Hình ảnh (URL, có thể upload file lên Cloudinary hoặc dán URL trực tiếp, chọn ảnh thumbnail)
- Biến thể màu – size (mỗi cặp màu-size duy nhất, không trùng lặp)

## Pre-Conditions
- Admin đã đăng nhập và có quyền quản trị sản phẩm.
- Danh mục đã được cấu hình sẵn trong hệ thống.

## Post-Conditions
- Thông tin sản phẩm, hình ảnh, biến thể hoặc trạng thái bán được lưu vào cơ sở dữ liệu.
- Danh sách sản phẩm phản ánh dữ liệu mới sau thao tác.

---

## Main Flow

### 1. Xem danh sách sản phẩm (với phân trang)
1. Actor mở trang **Quản lý sản phẩm** (`/admin/products`).
2. Hệ thống hiển thị danh sách sản phẩm theo phân trang:
   - Mặc định page = 1, size = 10
   - Sắp xếp theo `shoeId` giảm dần (mới nhất trước)
3. Mỗi dòng hiển thị: Tên, Thương hiệu, Danh mục, Giá cơ bản, Trạng thái bán (toggle switch).
4. Mỗi dòng có các nút: **Xem**, **Sửa**, **Bật/Tắt**.
5. Hệ thống hiển thị điều khiển phân trang: trang trước/sau, tổng số trang.

### 1.A. Phân trang
1. Admin chọn số trang hoặc thay đổi số bản ghi/trang.
2. Hệ thống truy vấn dữ liệu tương ứng với page và size.
3. Nếu page > totalPages và totalPages > 0:
   - Hệ thống redirect về trang cuối (page = totalPages)
4. UI cập nhật hiển thị danh sách của trang đã chọn.

### 1.B. Tìm kiếm & Lọc
1. Admin nhập từ khóa (tên/brand) và tùy chọn chọn bộ lọc: Danh mục, Thương hiệu, Trạng thái (Đang bán/Ngừng bán).
2. Admin nhấn **Tìm kiếm** hoặc Enter.
3. Hệ thống query database với điều kiện:
   - keyword: LIKE tìm trong name hoặc brand
   - categoryId: = categoryId (nếu có)
   - brand: = brand (nếu có)
   - status: = status (true/false, nếu có)
4. Hệ thống trả về danh sách sản phẩm thỏa điều kiện theo phân trang; có thể rỗng nếu không khớp.

### 1.C. Xem chi tiết (read-only)
1. Admin chọn nút **Xem** hoặc click tên sản phẩm.
2. Hệ thống truy vấn sản phẩm với category, images, variants (fetch eager với `findByIdForAdmin`).
3. Hệ thống hiển thị chi tiết:
   - Thông tin chung: ID, Tên, Thương hiệu, Loại giày, Giá, Mô tả, Bộ sưu tập
   - Danh mục
   - Hình ảnh (hiển thị tất cả, đánh dấu thumbnail)
   - Biến thể màu-size (hiển thị tất cả)
   - Trạng thái bán

### 2. Thêm sản phẩm mới
1. Admin chọn **Thêm sản phẩm mới** (`/admin/products/create`).
2. Hệ thống hiển thị form với các trường:
   - Tên sản phẩm* (bắt buộc)
   - Thương hiệu* (bắt buộc)
   - Loại giày* (dropdown: Nam/Nữ/Unisex, bắt buộc)
   - Giá cơ bản* (> 0, bắt buộc)
   - Mô tả
   - Bộ sưu tập
   - Danh mục* (dropdown, bắt buộc)
   - Hình ảnh (có thể thêm nhiều):
     - Dán URL trực tiếp, hoặc
     - Chọn file → Upload lên Cloudinary (folder `shoe_store_product`) → auto-fill URL
     - Chọn 1 ảnh làm thumbnail (radio button)
   - Biến thể (có thể thêm nhiều):
     - Màu* (dropdown Color enum)
     - Size* (input, tự động normalize thành SIZE_XX)
     - Nút **Thêm biến thể**
3. Admin nhập thông tin và nhấn **Lưu**.
4. Hệ thống kiểm tra validation:
   - Trường bắt buộc không để trống
   - Giá > 0
   - Danh mục tồn tại
   - Có ít nhất 1 biến thể hợp lệ (màu và size không trống)
   - Không trùng biến thể (check key = color + "-" + size)
   - Size được normalize (VD: "42" → "SIZE_42")
5. Nếu hợp lệ:
   - Lưu sản phẩm với status = true (Đang bán)
   - Lưu hình ảnh với URL (từ Cloudinary hoặc URL trực tiếp)
   - Lưu biến thể
   - Log: "Admin created product: {name} with ID: {shoeId}"
6. Hệ thống hiển thị thông báo "Thêm sản phẩm thành công" và redirect về danh sách.

### 3. Chỉnh sửa sản phẩm
1. Admin chọn nút **Sửa** tại danh sách hoặc trang chi tiết (`/admin/products/{id}/edit`).
2. Hệ thống load sản phẩm với images và variants, hiển thị form chỉnh sửa điền sẵn dữ liệu cũ.
3. Admin cập nhật các trường: Tên, Thương hiệu, Loại giày, Giá, Mô tả, Bộ sưu tập, Danh mục.
4. Admin có thể:
   - Thêm/xóa/sửa hình ảnh:
     - Giữ `imageId` cho ảnh cũ
     - Upload file mới lên Cloudinary hoặc dán URL mới
     - Thay đổi thumbnail
   - Thêm/xóa/sửa biến thể:
     - Giữ `variantId` cho biến thể cũ
     - Thêm biến thể mới
5. Admin nhấn **Lưu**.
6. Hệ thống kiểm tra validation như bước 2.4.
7. Nếu hợp lệ:
   - Cập nhật thông tin sản phẩm
   - Clear và replace hình ảnh (orphan removal)
   - Clear và replace biến thể (orphan removal)
   - Log: "Admin updated product ID: {shoeId}"
8. Hệ thống hiển thị thông báo "Cập nhật sản phẩm thành công" và redirect về danh sách.

### 4. Bật/Tắt trạng thái bán
1. Tại danh sách, Admin click toggle switch **Bật/Tắt** của sản phẩm.
2. Form submit request POST đến `/admin/products/{id}/toggle-status`.
3. Hệ thống toggle trạng thái: status = !status (true → false hoặc false → true).
4. Hệ thống lưu và log: "Admin toggled status for product ID: {shoeId} to {newStatus}".
5. Hiển thị thông báo "Đã cập nhật trạng thái sản phẩm".
6. Redirect về danh sách, UI refresh.

---

## Alternate Flow

### AF1. Yêu cầu trang vượt giới hạn
- Nếu Admin request page > totalPages và totalPages > 0:
  - Hệ thống tự động redirect về trang cuối (page = totalPages)
  - Hiển thị danh sách của trang cuối

### AF2. Không có kết quả tìm kiếm
- Khi bộ lọc/từ khóa không khớp sản phẩm nào:
  - Hệ thống trả về Page rỗng (totalElements = 0)
  - UI hiển thị thông báo "Không tìm thấy sản phẩm"
  - Admin có thể điều chỉnh điều kiện và tìm lại

### AF3. Sản phẩm không tồn tại khi xem/sửa/đổi trạng thái
- Hệ thống throw `NotFoundException`: "Không tìm thấy sản phẩm ID: {shoeId}"
- Hiển thị error message và redirect về danh sách

### AF4. Upload ảnh lên Cloudinary
- Admin chọn file ảnh → POST request đến `/admin/uploads/images`
- Hệ thống upload file lên Cloudinary folder `shoe_store_product`
- Return JSON với `url` của ảnh đã upload
- Auto-fill URL vào form input
- Nếu upload thất bại: Hiển thị error message "Không thể upload ảnh. Vui lòng thử lại."

---

## Exception Flow

### EF1. Dữ liệu không hợp lệ khi thêm/chỉnh sửa
- Thiếu trường bắt buộc (tên, brand, type, basePrice, categoryId)
- Giá ≤ 0
- Màu hoặc size của biến thể trống
- Trùng lặp biến thể (cùng color + size)
- Không có ít nhất 1 biến thể hợp lệ
- Binding validation errors được detect (BindingResult.hasErrors())
- Hệ thống giữ nguyên form, hiển thị lỗi validation, đánh dấu trường lỗi
- Hiển thị message: "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
- Không lưu thay đổi, yêu cầu admin nhập lại

### EF2. Danh mục không tồn tại
- Khi categoryId được chọn không tồn tại trong DB
- Hệ thống throw `NotFoundException`: "Không tìm thấy danh mục ID: {categoryId}"
- Hiển thị error message, giữ nguyên form

### EF3. Lỗi upload file
- Khi file rỗng hoặc upload failed (IOException)
- Return error response: `{"error": "Không thể upload ảnh. Vui lòng thử lại."}`
- Status 400 hoặc 500
- Admin có thể thử lại hoặc dán URL trực tiếp

### EF4. Lỗi lưu dữ liệu
- Khi có exception trong quá trình save (database error, constraint violation, etc.)
- Hệ thống catch exception, log error
- Hiển thị error message: `e.getMessage()`
- Giữ nguyên form để admin thử lại hoặc điều chỉnh

### EF5. Biến thể không hợp lệ
- Khi tất cả biến thể đều thiếu màu hoặc size (sau khi filter)
- Hệ thống throw `IllegalArgumentException`: "Vui lòng nhập ít nhất 1 biến thể hợp lệ (màu và size)"
- Hiển thị error message, không lưu

### EF6. Trùng lặp biến thể
- Khi có 2+ biến thể với cùng color và size
- Hệ thống throw `IllegalArgumentException`: "Biến thể {color}-{size} bị trùng lặp"
- Hiển thị error message, không lưu

---

## Technical Notes

### Size Normalization
- Admin có thể nhập size dạng số (VD: "42", "43") hoặc dạng enum (VD: "SIZE_42")
- Hệ thống tự động normalize:
  - Trim và uppercase
  - Nếu chưa có prefix "SIZE_" → extract số và thêm prefix
  - VD: "42" → "SIZE_42", " size_43 " → "SIZE_43"

### Image Management
- Hình ảnh được lưu bằng URL (Cloudinary hoặc URL public khác)
- Khi edit: Orphan removal → old images không còn trong collection sẽ bị xóa khỏi DB
- Có 2 cách thêm ảnh:
  1. Upload file → Cloudinary → auto-fill URL
  2. Dán URL trực tiếp (public URL)
- Phải chọn 1 ảnh làm thumbnail (isThumbnail = true)

### Validation Details
- Tên sản phẩm, brand, type, basePrice, categoryId: **Bắt buộc** (@NotBlank, @NotNull)
- BasePrice: Phải > 0 (@Positive)
- Biến thể: Ít nhất 1 biến thể hợp lệ, không trùng lặp
- Ngày tạo/cập nhật: Tự động (createdAt, updatedAt) - không cần admin nhập
