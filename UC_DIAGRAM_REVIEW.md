# Đánh giá Use Case Diagram hiện tại

## ✅ ĐÚNG - Các UC đã có trong diagram:

### **Admin - Quản lý Khuyến mãi:**
- ✅ **Manage Campaign** - Quản lý chiến dịch khuyến mãi
- ✅ **Manage Voucher** - Quản lý mã giảm giá
- ✅ **Manage Product** - Quản lý sản phẩm

### **Customer - Sử dụng Voucher:**
- ✅ **Apply voucher** - Áp dụng mã giảm giá (khi thanh toán)

### **Các UC khác:**
- ✅ Register, Login, Verify Email, Forgot Password
- ✅ Search Products, Sort Products, Filter Products
- ✅ View Product List, View Product Details
- ✅ Create Order, Payment, Manage Cart
- ✅ View Order History, Review Shoes
- ✅ Manage User Profile
- ✅ Manage Order, Manage Inventory
- ✅ View Sale Report, Revenue Statistics Report

---

## ⚠️ CẦN BỔ SUNG/SỬA - Theo code thực tế:

### **1. Customer - Voucher (cần bổ sung):**

Hiện tại chỉ có **"Apply voucher"**, nhưng theo code còn có:

```
Thêm UC: "View Voucher List" 
- Actor: Customer (Registered)
- Description: Xem danh sách mã giảm giá khả dụng
- Endpoint: GET /vouchers

Quan hệ:
- Customer → View Voucher List
- View Voucher List <<extend>> Apply voucher (xem voucher trước, sau đó áp dụng khi checkout)
```

**Sửa lại:**
- Tách "Apply voucher" thành 2 UC:
  1. **View Voucher List** (UC độc lập - xem danh sách voucher)
  2. **Apply Voucher** (UC trong quá trình checkout)
  
- Thêm quan hệ `<<include>>`:
  - "Create Order" `<<include>>` "Apply Voucher" (optional - có thể áp dụng hoặc không)

---

### **2. Admin - Manage Product (cần chi tiết hơn):**

Hiện tại có **"Manage Product"** (tổng quát), nhưng nên tách thành các UC con:

```
UC chính: "Manage Product"

Các UC con (<<include>>):
1. List Products (với phân trang, tìm kiếm, lọc)
2. View Product Detail
3. Create Product
4. Edit Product
5. Toggle Product Status (Bật/Tắt bán)

Lưu ý: KHÔNG có "Delete Product" (theo đặc tả UC23)
```

**Gợi ý vẽ:**
```
Admin → Manage Product
    Manage Product <<include>> List Products
    Manage Product <<include>> View Product Detail
    Manage Product <<include>> Create Product
    Manage Product <<include>> Edit Product
    Manage Product <<include>> Toggle Product Status
```

---

### **3. Admin - Manage Campaign (cần chi tiết hơn):**

Hiện tại có **"Manage Campaign"** (tổng quát), nên tách thành:

```
UC chính: "Manage Campaign"

Các UC con (<<include>>):
1. List Campaigns (với tìm kiếm, lọc)
2. View Campaign Detail (bao gồm xem danh sách voucher thuộc campaign)
3. Create Campaign
4. Edit Campaign
5. Toggle Campaign Status (Bật/Tắt)
6. Delete Campaign (có check: không thể xóa nếu có voucher)
```

**Gợi ý vẽ:**
```
Admin → Manage Campaign
    Manage Campaign <<include>> List Campaigns
    Manage Campaign <<include>> View Campaign Detail
    Manage Campaign <<include>> Create Campaign
    Manage Campaign <<include>> Edit Campaign
    Manage Campaign <<include>> Toggle Campaign Status
    Manage Campaign <<include>> Delete Campaign
```

---

### **4. Admin - Manage Voucher (cần chi tiết hơn):**

Hiện tại có **"Manage Voucher"** (tổng quát), nên tách thành:

```
UC chính: "Manage Voucher"

Các UC con (<<include>>):
1. List Vouchers (với tìm kiếm, lọc theo campaign)
2. View Voucher Detail
3. Create Voucher (phải chọn campaign, validate ngày trong phạm vi campaign)
4. Edit Voucher
5. Toggle Voucher Status (Bật/Tắt)
6. Delete Voucher (TODO: check không thể xóa nếu đã được dùng trong order)
```

**Gợi ý vẽ:**
```
Admin → Manage Voucher
    Manage Voucher <<include>> List Vouchers
    Manage Voucher <<include>> View Voucher Detail
    Manage Voucher <<include>> Create Voucher
    Manage Voucher <<include>> Edit Voucher
    Manage Voucher <<include>> Toggle Voucher Status
    Manage Voucher <<include>> Delete Voucher
```

---

### **5. Quan hệ giữa UC (cần bổ sung):**

**Quan hệ `<<extend>>`:**
```
1. "View Product List" <<extend>> "Sort Products"
   (Sắp xếp là optional, mở rộng từ xem danh sách)

2. "View Product List" <<extend>> "Filter Products"
   (Lọc là optional, mở rộng từ xem danh sách)

3. "Create Order" <<extend>> "Apply Voucher"
   (Áp dụng voucher là optional khi đặt hàng)

4. "View Voucher List" <<extend>> "Apply Voucher"
   (Có thể xem voucher rồi mới áp dụng khi checkout)
```

**Quan hệ `<<include>>`:**
```
1. "Create Order" <<include>> "Manage Cart"
   (Phải có cart trước khi tạo order)

2. "Payment" <<include>> "Create Order"
   (Payment bao gồm việc tạo order)

3. "Apply Voucher" <<include>> "Payment"
   (Áp dụng voucher là một phần của thanh toán)
```

---

## 📝 MÔ TẢ CHI TIẾT CÁC UC MỚI:

### **UC26: View Voucher List (Customer)**
```
Actor: Registered Customer
Pre-condition: Customer đã đăng nhập
Main Flow:
  1. Customer truy cập /vouchers
  2. Hệ thống hiển thị danh sách voucher khả dụng
  3. Mỗi voucher hiển thị: Mã, Tiêu đề, Điều kiện, Thời gian, Giảm giá
  4. Voucher đủ điều kiện: màu bình thường, có nút "Sao chép mã"
  5. Voucher chưa đủ điều kiện: màu xám, hiển thị lý do
```

### **UC23: Manage Product (Admin) - Chi tiết**
```
Actor: Admin
Pre-condition: Admin đã đăng nhập, có quyền quản lý sản phẩm

Sub-UCs:
1. List Products:
   - Phân trang (page, size=10)
   - Tìm kiếm (keyword: name/brand)
   - Lọc (categoryId, brand, status)
   - Sắp xếp (shoeId DESC)

2. View Product Detail:
   - Xem đầy đủ: thông tin, hình ảnh, biến thể màu-size
   - Read-only

3. Create Product:
   - Nhập: Tên, Brand, Type, BasePrice, Description, Collection, Category
   - Thêm hình ảnh (upload Cloudinary hoặc URL)
   - Thêm biến thể (màu-size, không trùng lặp)
   - Validation: trường bắt buộc, giá > 0, ít nhất 1 biến thể
   - Status mặc định = true (Đang bán)

4. Edit Product:
   - Cập nhật thông tin
   - Thêm/xóa/sửa hình ảnh (orphan removal)
   - Thêm/xóa/sửa biến thể (orphan removal)
   - Log: "Admin updated product ID: {id}"

5. Toggle Product Status:
   - Bật/Tắt trạng thái bán (status = !status)
   - POST /admin/products/{id}/toggle-status
   - Log: "Admin toggled status for product ID: {id} to {status}"

LƯU Ý: KHÔNG có chức năng Delete (hard delete)
```

### **UC24: Manage Campaign (Admin) - Chi tiết**
```
Actor: Admin
Pre-condition: Admin đã đăng nhập, có quyền quản lý khuyến mãi

Sub-UCs:
1. List Campaigns:
   - Tìm kiếm (keyword: tên)
   - Lọc (discountType, status, enabled)
   - Filter trên client-side

2. View Campaign Detail:
   - Xem thông tin chiến dịch
   - Xem đối tượng áp dụng (ALL/PRODUCT/CATEGORY)
   - Xem danh sách voucher thuộc campaign

3. Create Campaign:
   - Nhập: Tên, Mô tả, Thời gian, DiscountType, DiscountValue, Max/Min
   - Chọn đối tượng: ALL / PRODUCT (chọn shoes) / CATEGORY (chọn categories)
   - Validation: trường bắt buộc, giá trị > 0, endDate >= startDate
   - Status tự động tính: enabled + ngày hiện tại → DRAFT/ACTIVE/ENDED/CANCELLED

4. Edit Campaign:
   - Cập nhật thông tin
   - Xóa targets cũ, lưu targets mới
   - TỰ ĐỘNG điều chỉnh vouchers liên kết:
     * Điều chỉnh ngày voucher nếu nằm ngoài phạm vi campaign
     * Tắt voucher nếu ngày không hợp lệ
     * Đồng bộ discount rules từ campaign sang voucher

5. Toggle Campaign Status:
   - Bật/Tắt enabled
   - Status tự động cập nhật

6. Delete Campaign:
   - Check: có voucher không? (existsByCampaign_CampaignId)
   - Nếu có → throw "Chiến dịch có voucher, không thể xóa"
   - Nếu không → xóa campaign + targets (cascade)
```

### **UC25: Manage Voucher (Admin) - Chi tiết**
```
Actor: Admin
Pre-condition: Admin đã đăng nhập, có quyền quản lý khuyến mãi, có ít nhất 1 campaign

Sub-UCs:
1. List Vouchers:
   - Tìm kiếm (keyword: code/title)
   - Lọc (campaignId, discountType, enabled)
   - Eager fetch campaign

2. View Voucher Detail:
   - Xem thông tin voucher
   - Xem campaign liên kết
   - Xem quy tắc giảm giá

3. Create Voucher:
   - Nhập: Code (unique), Title, Description, Campaign, DiscountType, DiscountValue
   - Max/Min (nếu không nhập → fallback từ campaign hoặc = 0)
   - Ngày phải nằm trong phạm vi campaign
   - MaxRedeemPerCustomer (số lần dùng/khách)
   - Validation: code unique, giá trị > 0, ngày trong campaign

4. Edit Voucher:
   - Cập nhật thông tin (trừ code - không cho sửa)
   - Validation tương tự Create

5. Toggle Voucher Status:
   - Bật/Tắt enabled

6. Delete Voucher:
   - TODO: Check không thể xóa nếu đã được dùng trong order
   - (Hiện tại chưa implement check này)
```

---

## 🎨 GỢI Ý VẼ LAI USE CASE DIAGRAM:

### **Cấu trúc tổng thể:**

```
┌─────────────────────────────────────────────────────────────┐
│                   SHOES SELLING WEBSITE                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [Unregistered Customer] ──→ Register                        │
│         │                    ├─→ Verify Email                │
│         │                    └─→ Forgot Password             │
│         ├──→ Search Products                                 │
│         ├──→ View Product List ─┬─→ <<extend>> Sort          │
│         │                       └─→ <<extend>> Filter        │
│         └──→ View Product Details                            │
│                                                               │
│  [Customer] (generalization of Unregistered)                 │
│         ├──→ Login                                            │
│         ├──→ View Voucher List ──→ <<extend>> Apply Voucher  │
│         ├──→ Manage Cart                                      │
│         ├──→ Create Order ─┬─→ <<include>> Manage Cart       │
│         │                  └─→ <<extend>> Apply Voucher      │
│         ├──→ Payment ──→ <<include>> Create Order            │
│         ├──→ View Order History                              │
│         ├──→ Review Shoes                                     │
│         └──→ Manage User Profile                             │
│                                                               │
│  [Admin] ──→ Manage Product ─┬─→ List Products               │
│         │                     ├─→ View Detail                │
│         │                     ├─→ Create                     │
│         │                     ├─→ Edit                       │
│         │                     └─→ Toggle Status              │
│         │                                                     │
│         ├──→ Manage Campaign ─┬─→ List Campaigns             │
│         │                      ├─→ View Detail               │
│         │                      ├─→ Create                    │
│         │                      ├─→ Edit                      │
│         │                      ├─→ Toggle Status             │
│         │                      └─→ Delete                    │
│         │                                                     │
│         ├──→ Manage Voucher ─┬─→ List Vouchers               │
│         │                     ├─→ View Detail                │
│         │                     ├─→ Create                     │
│         │                     ├─→ Edit                       │
│         │                     ├─→ Toggle Status              │
│         │                     └─→ Delete                     │
│         │                                                     │
│         ├──→ Manage Order                                    │
│         ├──→ Manage Inventory                                │
│         ├──→ View Sale Report                                │
│         └──→ Revenue Statistics Report                       │
│                                                               │
│  [Payment Gateway] ──→ Payment (external actor)              │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 TÓM TẮT THAY ĐỔI:

### **Cần bổ sung vào diagram:**
1. ✅ **View Voucher List** (UC mới cho Customer)
2. ✅ Chi tiết các UC con của **Manage Product** (5 UC con)
3. ✅ Chi tiết các UC con của **Manage Campaign** (6 UC con)
4. ✅ Chi tiết các UC con của **Manage Voucher** (6 UC con)

### **Quan hệ cần bổ sung:**
- `<<extend>>`: View Product List → Sort/Filter
- `<<extend>>`: Create Order → Apply Voucher
- `<<extend>>`: View Voucher List → Apply Voucher
- `<<include>>`: Create Order → Manage Cart
- `<<include>>`: Payment → Create Order

### **Lưu ý:**
- **Không có Delete Product** trong Manage Product
- **Manage Campaign** có Delete (nhưng check có voucher)
- **Manage Voucher** có Delete (TODO: check đã dùng trong order)
- **Apply Voucher** có thể gọi từ 2 nơi: View Voucher List hoặc Create Order

---

## ✍️ KẾT LUẬN:

Use Case Diagram hiện tại đã **CƠ BẢN ĐÚNG** về tổng thể, nhưng cần:
1. **Tách chi tiết** các UC tổng quát (Manage Product, Campaign, Voucher)
2. **Bổ sung UC mới**: View Voucher List cho Customer
3. **Thêm quan hệ** <<include>>, <<extend>> cho rõ ràng
4. **Cập nhật mô tả** cho đúng với code thực tế

Diagram hiện tại phù hợp để **High-level overview**, nhưng nếu cần **chi tiết** thì nên vẽ thêm các sub-diagram cho từng module.
