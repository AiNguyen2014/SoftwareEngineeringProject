# Use Case [23]: Quản lý sản phẩm (Admin)

## Actor(s)
- **ADMIN**

## Trigger
- Admin truy cập mục **Quản lý sản phẩm** trên trang quản trị.

## Description
Admin quản lý danh sách sản phẩm giày: xem/lọc/tìm kiếm, xem chi tiết, tạo mới, chỉnh sửa và bật/tắt trạng thái bán. Admin **không thao tác tồn kho**; tồn kho được xử lý bởi module khác. Không hỗ trợ xóa sản phẩm.

Thông tin sản phẩm có thể cập nhật:
- Tên sản phẩm, thương hiệu, loại giày (Nam/Nữ/Unisex)
- Giá cơ bản, mô tả, bộ sưu tập (collection)
- Danh mục
- Hình ảnh (chọn ảnh thumbnail)
- Biến thể màu – size (mỗi cặp màu-size duy nhất)

## Pre-Conditions
- Admin đã đăng nhập và có quyền quản trị sản phẩm.
- Danh mục đã được cấu hình sẵn.

## Post-Conditions
- Thông tin sản phẩm, hình ảnh, biến thể hoặc trạng thái bán được lưu vào cơ sở dữ liệu.
- Danh sách sản phẩm phản ánh dữ liệu mới sau thao tác.

---

## Main Flow

### 1. Xem danh sách sản phẩm
1. Actor mở trang **Quản lý sản phẩm**.
2. Hệ thống hiển thị danh sách theo phân trang (mặc định sắp xếp mới nhất theo `shoeId` giảm dần).
3. Mỗi dòng hiển thị: Tên, Thương hiệu, Danh mục, Giá cơ bản, Trạng thái bán.

### 1.A. Phân trang
1. Hệ thống hiển thị điều khiển trang trước/sau, nhập số trang, chọn số bản ghi/trang.
2. Admin chọn trang khác.
3. Hệ thống truy vấn và trả về danh sách của trang đã chọn; UI cập nhật kết quả.

### 1.B. Tìm kiếm & Lọc
1. Admin nhập từ khóa (tên/brand) và tùy chọn chọn bộ lọc: Danh mục, Thương hiệu, Trạng thái (Đang bán/Ngừng bán).
2. Admin nhấn **Tìm kiếm**.
3. Hệ thống trả về danh sách sản phẩm thỏa điều kiện; có thể rỗng nếu không khớp.

### 1.C. Xem chi tiết (read-only)
1. Admin chọn một sản phẩm trong danh sách.
2. Hệ thống hiển thị chi tiết: thông tin chung, danh mục, giá, mô tả, hình ảnh (thumbnail), biến thể màu-size.
3. Tồn kho không hiển thị và không cho chỉnh sửa trong màn này.

### 2. Thêm sản phẩm mới
1. Admin chọn **Thêm sản phẩm mới**.
2. Admin nhập thông tin bắt buộc: Tên, Thương hiệu, Loại giày, Giá > 0, Danh mục. Có thể thêm mô tả, bộ sưu tập.
3. Admin thêm hình ảnh (đánh dấu 1 ảnh thumbnail) và các biến thể màu-size (mỗi cặp màu-size duy nhất). Có 2 cách thêm ảnh:
	- Dán URL ảnh công khai, hoặc
	- Chọn file ảnh → hệ thống upload lên Cloudinary (folder `shoe_store_product`) và tự điền URL trả về.
4. Hệ thống kiểm tra: trường bắt buộc, giá hợp lệ, danh mục tồn tại, không trùng biến thể.
5. Nếu hợp lệ, hệ thống lưu sản phẩm với trạng thái **Đang bán**; tồn kho của biến thể mặc định 0 và không cho nhập. Ảnh được lưu bằng URL trong bảng `shoes_image`.
6. Hệ thống hiển thị thông báo thành công và quay lại danh sách.

### 3. Chỉnh sửa sản phẩm
1. Admin mở sản phẩm cần chỉnh sửa.
2. Admin cập nhật các trường: Tên, Thương hiệu, Loại giày, Giá, Mô tả, Bộ sưu tập, Danh mục; thêm/xóa/sửa hình ảnh (giữ `imageId` cho ảnh cũ, có thể upload file mới lên Cloudinary hoặc dán URL); thêm/xóa/sửa biến thể màu-size (giữ `variantId` cho biến thể cũ).
3. Hệ thống kiểm tra như bước 2.4 và đảm bảo không trùng biến thể.
4. Tồn kho biến thể được giữ nguyên, không hiển thị để chỉnh sửa.
5. Nếu hợp lệ, hệ thống lưu và báo thành công rồi quay về danh sách.

### 4. Bật/Tắt trạng thái bán
1. Tại danh sách, Admin nhấn nút **Bật/Tắt** trạng thái của sản phẩm.
2. Hệ thống đảo trạng thái: Đang bán ↔ Ngừng bán, lưu kết quả và hiển thị thông báo.

---

## Alternate Flow

### AF1. Yêu cầu trang vượt giới hạn
- Nếu Admin nhập trang > tổng số trang, hệ thống chuyển về trang hợp lệ gần nhất (trang cuối) và hiển thị danh sách tương ứng.

### AF2. Không có kết quả tìm kiếm
- Khi bộ lọc/từ khóa không khớp sản phẩm nào, hệ thống trả về danh sách rỗng; Admin có thể điều chỉnh điều kiện và tìm lại.

### AF3. Sản phẩm không tồn tại khi xem/sửa/đổi trạng thái
- Hệ thống thông báo lỗi "Không tìm thấy sản phẩm" và chuyển về danh sách.

---

## Exception Flow

### EF1. Dữ liệu không hợp lệ khi thêm/chỉnh sửa
- Thiếu trường bắt buộc, giá ≤ 0, màu/size trống hoặc trùng lặp.
- Hệ thống giữ nguyên form, đánh dấu lỗi và yêu cầu nhập lại.

### EF2. Lỗi lưu dữ liệu
- Khi có lỗi hệ thống khác, hệ thống hiển thị thông báo chung và không lưu thay đổi.
