# Đặc Tả Use Case - WebShoe Shoes Module

## 📋 Danh Sách Use Case

| ID | Use Case | Actor | Mức độ ưu tiên |
|----|---------|----|---|
| UC-1 | View Shoe List | Customer | High |
| UC-2 | View Shoe Detail | Customer | High |
| UC-3 | Search Products | Customer | Medium |
| UC-4 | Filter by Category | Customer | Medium |
| UC-5 | Filter by Type | Customer | Medium |
| UC-6 | Add to Cart | Customer | High |
| UC-7 | View Related Products | Customer | Low |

---

## UC-1: View Shoe List

### **Thông tin chung**
- **ID**: UC-1
- **Tên**: View Shoe List
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng xem danh sách sản phẩm giày đã phân trang
- **Mức độ ưu tiên**: High
- **Tần suất sử dụng**: Very High

### **Điều kiện tiên quyết (Preconditions)**
- Hệ thống đã khởi động
- Database chứa ít nhất 1 sản phẩm
- Kết nối mạng hoạt động

### **Luồng chính (Main Flow)**

| Bước | Actor | Hệ thống |
|------|-------|---------|
| 1 | Khách hàng truy cập URL `/` hoặc `/` với tham số `page=X&size=Y` | |
| 2 | | Nhận request GET từ ShoesController.homePage(page, size) |
| 3 | | Gọi ShoesService.getShoesList(page, size) |
| 4 | | Tạo Pageable(page-1, size) |
| 5 | | Gọi ShoesRepository.findAll(pageable) |
| 6 | | Truy vấn Database: SELECT DISTINCT s FROM Shoes s LEFT JOIN FETCH s.category LEFT JOIN FETCH s.images |
| 7 | | Database trả về Page<Shoes> với các sản phẩm (mặc định 12 sản phẩm trên trang) |
| 8 | | Loop qua từng Shoes, gọi convertToSummaryDto(shoes) |
| 9 | | Cho mỗi sản phẩm: gọi getThumbnailUrl(shoes) để lấy ảnh thumbnail |
| 10 | | Cho mỗi sản phẩm: gọi isOutOfStock(shoeId) |
| 11 | | isOutOfStock → gọi variantRepository.getTotalStockByShoeId(shoeId) |
| 12 | | Database tính tổng tồn kho: SELECT SUM(v.stock) FROM ShoesVariant v WHERE v.shoes.shoeId = :shoeId |
| 13 | | Xây dựng ShoesSummaryDto với (shoeId, name, brand, price, thumbnailUrl, outOfStock, type) |
| 14 | | Xây dựng ShoesListDto với (products, currentPage, totalPages, totalItems) |
| 15 | | Thêm dữ liệu vào Model: addAttribute("products", ...) |
| 16 | | Render template shoes-list.html |
| 17 | | Template Thymeleaf loop qua products và hiển thị card grid |
| 18 | | Template sinh ra pagination links (page 1, 2, 3, ...) |
| 19 | | Trả về HTML response |
| 20 | Khách hàng nhìn thấy danh sách giày dưới dạng grid với phân trang | |

### **Luồng thay thế (Alternative Flows)**

**AF-1: Trang trống (No products)**
- Ở bước 7: Database trả về danh sách rỗng
- Ở bước 18: Template hiển thị "No products found" message

**AF-2: Trang không hợp lệ**
- Ở bước 2: Nếu `page < 1` hoặc `size < 1`
- System: Reset về page=1, size=12
- Tiếp tục luồng chính

**AF-3: Page quá cao**
- Ở bước 7: Database trả về danh sách rỗng vì page vượt quá totalPages
- Template hiển thị danh sách trống hoặc redirect về trang 1

### **Điều kiện kết thúc thành công (Postconditions)**
- Khách hàng nhìn thấy danh sách giày được phân trang đúng cách
- Mỗi sản phẩm hiển thị: ảnh, tên, brand, giá, trạng thái tồn kho
- Các link phân trang hoạt động chính xác

### **Ngoại lệ (Exceptions)**
| Ngoại lệ | Xử lý |
|---------|-------|
| Database không khả dụng | Hiển thị error message "Unable to load products" |
| Timeout query | Hiển thị error page |
| Network error | Browser retry hoặc error message |

### **Ghi chú hiệu suất**
- **Query count**: 13 (1 main + 12 stock queries) → **N+1 Problem**
- **Performance**: ~150ms (cần optimize)
- **Recommended**: Batch load stocks thay vì query từng cái

### **Data được sử dụng**
- **Input**: page (int), size (int)
- **Output**: ShoesListDto
  ```json
  {
    "products": [
      {
        "shoeId": 1,
        "name": "Nike Air Jordan",
        "brand": "Nike",
        "price": 120.99,
        "thumbnailUrl": "https://...",
        "outOfStock": false,
        "type": "CASUAL"
      },
      ...
    ],
    "currentPage": 1,
    "totalPages": 2,
    "totalItems": 24
  }
  ```

---

## UC-2: View Shoe Detail

### **Thông tin chung**
- **ID**: UC-2
- **Tên**: View Shoe Detail
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng xem thông tin chi tiết một sản phẩm giày kèm ảnh, variant, sản phẩm liên quan
- **Mức độ ưu tiên**: High
- **Tần suất sử dụng**: Very High

### **Điều kiện tiên quyết (Preconditions)**
- Hệ thống đã khởi động
- Sản phẩm với ID đó tồn tại trong database
- Kết nối mạng hoạt động
- Khách hàng đã xem danh sách (từ UC-1) hoặc nhập URL trực tiếp

### **Luồng chính (Main Flow)**

| Bước | Actor | Hệ thống |
|------|-------|---------|
| 1 | Khách hàng click vào một sản phẩm từ danh sách (hoặc truy cập `/product/{id}`) | |
| 2 | | Nhận request GET /product/1 |
| 3 | | Gọi ShoesController.productDetail(id=1) |
| 4 | | Gọi ShoesService.getShoesDetail(shoeId=1) |
| 5 | | Gọi ShoesRepository.findByIdWithDetails(shoeId=1) |
| 6 | | Truy vấn Database: SELECT s FROM Shoes s LEFT JOIN FETCH s.category LEFT JOIN FETCH s.images LEFT JOIN FETCH s.variants WHERE s.shoeId = 1 |
| 7 | | Database trả về Optional<Shoes> với tất cả relationships (category, images, variants) |
| 8 | | Kiểm tra Optional - nếu rỗng → throw NotFoundException |
| 9 | | Gọi convertToDetailDto(shoes) |
| 10 | | Trích xuất category name từ shoes.getCategory().getName() |
| 11 | | Loop qua shoes.getImages() → xây dựng List<String> imageUrls |
| 12 | | Trong loop: kiểm tra img.isThumbnail() để set thumbnailUrl |
| 13 | | Loop qua shoes.getVariants() → collect sizes, colors, và tính totalStock |
| 14 | | Cho mỗi variant: variant.getSizeValue(), variant.getColorValue(), variant.getStock() |
| 15 | | Gọi getRelatedProducts(shoes) |
| 16 | | Kiểm tra shoes.getCategory() không null |
| 17 | | Gọi ShoesRepository.findRelatedProducts(categoryId, excludeShoeId, pageable=5) |
| 18 | | Database truy vấn: SELECT DISTINCT s FROM Shoes s LEFT JOIN FETCH s.category LEFT JOIN FETCH s.images WHERE s.category.categoryId = :categoryId AND s.shoeId <> :excludeShoeId ORDER BY s.createdAt DESC LIMIT 5 |
| 19 | | Database trả về Page<Shoes> với 5 sản phẩm liên quan tối đa |
| 20 | | Loop qua 5 related shoes, gọi convertToSummaryDto cho mỗi cái |
| 21 | | Cho mỗi related shoe: gọi isOutOfStock() → getTotalStockByShoeId() |
| 22 | | Xây dựng ShoesDetailDto với (shoeId, name, brand, basePrice, description, category, type, collection, imageUrls, sizes, colors, totalStock, relatedProducts) |
| 23 | | Model.addAttribute("product", product) |
| 24 | | Render template shoes-detail.html |
| 25 | | Template hiển thị: |
|    | | - Image gallery (từ imageUrls) |
|    | | - Product name, brand, price, description |
|    | | - Category, type, collection badges |
|    | | - Available sizes dropdown (từ sizes set) |
|    | | - Available colors dropdown (từ colors set) |
|    | | - Total stock indicator |
|    | | - Add to cart form với variant selector |
|    | | - Related products section (carousel/grid) |
| 26 | Khách hàng xem thông tin chi tiết sản phẩm, chọn size/color, có thể add to cart | |

### **Luồng thay thế (Alternative Flows)**

**AF-1: Sản phẩm không tồn tại**
- Ở bước 8: Optional rỗng
- System: Throw NotFoundException
- Controller catch exception → Redirect `/`
- Khách hàng quay về trang danh sách

**AF-2: Sản phẩm không có hình ảnh**
- Ở bước 12: imageUrls rỗng
- System: Thêm placeholder image "https://placehold.co/600x600?text=No+Image"

**AF-3: Sản phẩm không có category**
- Ở bước 10: shoes.getCategory() == null
- System: Set categoryName = "General"

**AF-4: Sản phẩm hết hàng (totalStock <= 0)**
- Ở bước 13-14: totalStock = 0
- Template: Hiển thị "Out of Stock" badge, disable Add to Cart button

**AF-5: Không có sản phẩm liên quan**
- Ở bước 19: relatedPage.getContent() rỗng
- System: Trả về relatedProducts = [] (empty list)
- Template: Ẩn "Related Products" section hoặc hiển thị "No related products"

**AF-6: Error loading related products**
- Ở bước 21: Exception xảy ra khi fetch related products
- System: Log warning, return empty list []
- Template: Ẩn related products section

### **Điều kiện kết thúc thành công (Postconditions)**
- Khách hàng thấy chi tiết sản phẩm hoàn chỉnh
- Có thể chọn size/color variant
- Có thể xem các sản phẩm liên quan
- Ready to add to cart

### **Ngoại lệ (Exceptions)**
| Ngoại lệ | Xử lý |
|---------|-------|
| Sản phẩm không tìm thấy (ID sai) | NotFoundException → Redirect `/` |
| Database không khả dụng | Error page 500 |
| Timeout query | Error page 504 |
| Related products query fail | Log warning, skip related products |

### **Ghi chú hiệu suất**
- **Query count**: 7 (1 main + 1 related + 5 stock queries) → **N+1 Problem**
- **Performance**: ~80ms
- **Recommended**: Batch load stocks cho related products

### **Data được sử dụng**
- **Input**: shoeId (Long)
- **Output**: ShoesDetailDto
  ```json
  {
    "shoeId": 1,
    "name": "Nike Air Jordan Retro 1",
    "brand": "Nike",
    "basePrice": 120.99,
    "description": "Classic basketball shoe...",
    "category": "Casual",
    "type": "CASUAL",
    "collection": "Air Jordan",
    "imageUrls": [
      "https://...",
      "https://..."
    ],
    "sizes": ["40", "41", "42", "43", "44", "45"],
    "colors": ["Black", "White", "Red"],
    "totalStock": 45,
    "relatedProducts": [
      {
        "shoeId": 2,
        "name": "Nike Air Jordan Retro 2",
        ...
      },
      ...
    ]
  }
  ```

---

## UC-3: Search Products

### **Thông tin chung**
- **ID**: UC-3
- **Tên**: Search Products
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng tìm kiếm sản phẩm theo từ khóa
- **Mức độ ưu tiên**: Medium
- **Tần suất sử dụng**: High
- **Status**: Planned (Not yet implemented in current version)

### **Luồng chính (Main Flow)**
1. Khách hàng nhập từ khóa tìm kiếm và nhấn Search button
2. Browser gửi GET request `/search?keyword=nike&page=1`
3. ShoesController.search(keyword, page) xử lý request
4. ShoesService.searchShoes(keyword, page, size) gọi
5. ShoesRepository.searchByKeyword(keyword, pageable) truy vấn:
   ```sql
   SELECT DISTINCT s FROM Shoes s 
   LEFT JOIN FETCH s.category 
   LEFT JOIN FETCH s.images 
   WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
   OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
   ```
6. Batch load stocks cho tất cả kết quả
7. Render shoes-list.html với kết quả tìm kiếm

---

## UC-4: Filter by Category

### **Thông tin chung**
- **ID**: UC-4
- **Tên**: Filter by Category
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng lọc sản phẩm theo danh mục
- **Mức độ ưu tiên**: Medium
- **Tần suất sử dụng**: Medium
- **Status**: Planned (Not yet implemented in current version)

### **Luồng chính (Main Flow)**
1. Khách hàng click vào category link (e.g., "Casual Shoes")
2. Browser gửi GET request `/category/1?page=1`
3. ShoesController.getByCategory(categoryId, page) xử lý
4. ShoesService.getShoesByCategory(categoryId, page, size) gọi
5. ShoesRepository.findByCategory(categoryId, pageable) truy vấn
6. Batch load stocks
7. Render shoes-list.html với sản phẩm của category đó

---

## UC-5: Filter by Type

### **Thông tin chung**
- **ID**: UC-5
- **Tên**: Filter by Type
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng lọc sản phẩm theo loại giày
- **Mức độ ưu tiên**: Medium
- **Status**: Planned (Not yet implemented in current version)

### **Luồng chính (Main Flow)**
1. Khách hàng click vào type filter (e.g., "CASUAL", "BASKETBALL")
2. Browser gửi GET request `/type/CASUAL?page=1`
3. ShoesController.getByType(type, page) xử lý
4. ShoesService.getShoesByType(type, page, size) gọi
5. ShoesRepository.findByType(type, pageable) truy vấn
6. Batch load stocks
7. Render shoes-list.html

---

## UC-6: Add to Cart

### **Thông tin chung**
- **ID**: UC-6
- **Tên**: Add to Cart
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng thêm sản phẩm vào giỏ hàng
- **Mức độ ưu tiên**: High
- **Status**: Form UI Ready, Backend Endpoint Missing

### **Luồng chính (Main Flow)**
1. Khách hàng xem chi tiết sản phẩm (UC-2)
2. Chọn size từ dropdown
3. Chọn color từ dropdown
4. Nhập quantity (mặc định = 1)
5. Click "Add to Cart" button
6. Form submit POST request `/cart/add` với:
   ```json
   {
     "shoeId": 1,
     "size": "42",
     "color": "Black",
     "quantity": 1
   }
   ```
7. [Backend endpoint chưa được implement]
8. Giỏ hàng được cập nhật

### **Status**: ⏳ Planned

---

## UC-7: View Related Products

### **Thông tin chung**
- **ID**: UC-7
- **Tên**: View Related Products
- **Actor chính**: Customer
- **Mục tiêu**: Khách hàng xem các sản phẩm liên quan khi đang xem chi tiết
- **Mức độ ưu tiên**: Low
- **Tần suất sử dụng**: Medium

### **Luồng chính (Main Flow)**
- Included use case trong UC-2 (View Shoe Detail)
- Tự động hiển thị 5 sản phẩm trong cùng category
- Gọi getRelatedProducts(shoes) từ ShoesService
- Hiển thị dưới dạng carousel hoặc grid
- Khách hàng có thể click để xem chi tiết sản phẩm liên quan

---

## 📊 Tóm tắt Use Cases

| ID | Use Case | Status | Query Count | Performance |
|-----|---------|--------|------------|-------------|
| UC-1 | View Shoe List | ✅ Implemented | 13 (N+1) | ~150ms |
| UC-2 | View Shoe Detail | ✅ Implemented | 7 (N+1) | ~80ms |
| UC-3 | Search Products | ⏳ Planned | - | - |
| UC-4 | Filter by Category | ⏳ Planned | - | - |
| UC-5 | Filter by Type | ⏳ Planned | - | - |
| UC-6 | Add to Cart | 🔴 Form Ready, No Backend | - | - |
| UC-7 | View Related Products | ✅ Implemented (in UC-2) | 5 | ~40ms |

---

## 🎯 Khuyến nghị tiếp theo

### Phase 1 (Critical)
- Fix N+1 query problem bằng batch loading
- Move database credentials to environment variables

### Phase 2 (High Priority)
- Implement UC-3 (Search)
- Implement UC-4 (Filter by Category)
- Implement UC-5 (Filter by Type)
- Implement UC-6 (Add to Cart) endpoint
- Add input validation

### Phase 3 (Medium Priority)
- Implement pagination UI generation
- Add sorting capability
- Implement shopping cart functionality
- Add user authentication

### Phase 4 (Low Priority)
- Add product reviews/ratings
- Implement wishlist
- Add analytics tracking

