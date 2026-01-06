# 🎬 KỊCH BẢN THUYẾT TRÌNH CHI TIẾT
## Chức năng: View Shoe List & View Shoe Detail

---

## 📋 THÔNG TIN CHUNG

| Thông tin | Chi tiết |
|-----------|----------|
| **Người thuyết trình** | [Tên sinh viên] |
| **MSSV** | [Mã số sinh viên] |
| **Chức năng** | View Shoe List & View Shoe Detail |
| **Thời lượng** | 10-15 phút |
| **Số slide đề xuất** | 12 slides |

---

# 🎬 SLIDE 1: TRANG BÌA
## ⏱️ Thời gian: 30 giây

### 📺 Nội dung trên slide:
```
╔════════════════════════════════════════════════════════════╗
║                                                            ║
║     CHỨC NĂNG: XEM DANH SÁCH GIÀY & XEM CHI TIẾT GIÀY     ║
║                                                            ║
║     ─────────────────────────────────────────────────      ║
║                                                            ║
║     Người thực hiện: [Tên sinh viên]                       ║
║     MSSV: [Mã số sinh viên]                                ║
║     Môn học: Software Engineering Project                  ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

### 🎤 Lời nói:
> "Xin chào thầy/cô và các bạn. Em là [Tên], MSSV [Mã số]. Hôm nay em sẽ trình bày về 2 chức năng mà em đảm nhận trong dự án Shoe Store, đó là **Xem danh sách giày** và **Xem chi tiết giày**."

---

# 🎬 SLIDE 2: TỔNG QUAN CHỨC NĂNG
## ⏱️ Thời gian: 1 phút

### 📺 Nội dung trên slide:

| Chức năng | URL | Mô tả |
|-----------|-----|-------|
| **View Shoe List** | `GET /` | Trang chủ - Danh sách sản phẩm |
| **View Shoe Detail** | `GET /product/{id}` | Trang chi tiết sản phẩm |

**Actor:** Cả khách hàng đã đăng ký và chưa đăng ký đều có thể sử dụng

### 🎤 Lời nói:
> "Đây là 2 chức năng cốt lõi của một website thương mại điện tử.
> 
> **Chức năng thứ nhất - View Shoe List**: Khi người dùng truy cập trang chủ, hệ thống sẽ hiển thị danh sách tất cả sản phẩm giày dưới dạng card, có phân trang, mỗi trang 12 sản phẩm.
> 
> **Chức năng thứ hai - View Shoe Detail**: Khi người dùng click vào một sản phẩm bất kỳ, hệ thống sẽ hiển thị đầy đủ thông tin chi tiết như hình ảnh, mô tả, size, màu sắc, và các sản phẩm liên quan.
> 
> Cả 2 chức năng này đều phục vụ cho cả khách chưa đăng ký lẫn đã đăng ký."

---

# 🎬 SLIDE 3: KIẾN TRÚC HỆ THỐNG
## ⏱️ Thời gian: 1 phút

### 📺 Nội dung trên slide:
```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│         shoes-list.html  |  shoes-detail.html           │
└───────────────────────────┬─────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                      │
│                   ShoesController.java                   │
└───────────────────────────┬─────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                        │
│                    ShoesService.java                     │
└───────────────────────────┬─────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                      │
│                   ShoesRepository.java                   │
└───────────────────────────┬─────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────┐
│                       DATABASE                           │
│    shoes | shoes_image | shoes_variant | category        │
└─────────────────────────────────────────────────────────┘
```

### 🎤 Lời nói:
> "Dự án được xây dựng theo mô hình **MVC** kết hợp **Layered Architecture**, bao gồm 5 layer:
> 
> 1. **Presentation Layer**: Gồm các file template Thymeleaf để hiển thị giao diện
> 2. **Controller Layer**: Xử lý các HTTP request từ người dùng
> 3. **Service Layer**: Chứa business logic, xử lý dữ liệu
> 4. **Repository Layer**: Thực hiện các truy vấn đến database
> 5. **Database**: Lưu trữ dữ liệu trong PostgreSQL
> 
> Luồng dữ liệu sẽ đi từ trên xuống dưới khi request, và từ dưới lên trên khi response."

---

# 🎬 SLIDE 4: SEQUENCE DIAGRAM - VIEW SHOE LIST
## ⏱️ Thời gian: 1 phút 30 giây

### 📺 Nội dung trên slide:
*(Chèn hình Sequence Diagram từ file `01_ViewShoeList_Sequence.puml`)*

### 🎤 Lời nói:
> "Đây là Sequence Diagram của chức năng View Shoe List. Em sẽ giải thích từng bước:
> 
> **Bước 1**: Người dùng truy cập trang chủ, giao diện gửi request `GET /` đến Controller
> 
> **Bước 2**: Controller gọi method `getShoesList(page, size)` của Service
> 
> **Bước 3**: Service thực hiện **2 query** đến database:
> - Query 1: `findAllPaged()` - Lấy danh sách sản phẩm có phân trang
> - Query 2: `findAllByIdsWithImages()` - Lấy hình ảnh của các sản phẩm
> 
> **Bước 4**: Dữ liệu được chuyển đổi sang DTO và trả về cho giao diện hiển thị
> 
> Tổng cộng chỉ có **2 database query** để hiển thị trang danh sách."

---

# 🎬 SLIDE 5: CODE - CONTROLLER (View Shoe List)
## ⏱️ Thời gian: 1 phút 30 giây

### 📺 Nội dung trên slide:

**📁 File: `ShoesController.java`**

```java
@GetMapping("/")
public String homePage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int size,
        Model model) {
    
    // Gọi Service để lấy danh sách sản phẩm
    ShoesListDto data = shoesService.getShoesList(page, size);

    // Đẩy dữ liệu vào Model
    model.addAttribute("products", data.getProducts());
    model.addAttribute("currentPage", data.getCurrentPage());
    model.addAttribute("totalPages", data.getTotalPages());

    return "shoe/shoes-list";
}
```

### 🎤 Lời nói:
> "Đây là code của Controller. Em sẽ giải thích từng phần:
> 
> **Dòng 1**: Annotation `@GetMapping("/")` cho biết method này xử lý request GET đến đường dẫn gốc, tức là trang chủ.
> 
> **Dòng 2-4**: Hai tham số `page` và `size` được lấy từ URL query string. Annotation `@RequestParam(defaultValue = "1")` nghĩa là nếu không truyền thì mặc định page = 1, size = 12.
> 
> **Dòng 7**: Gọi method `getShoesList()` của Service để lấy dữ liệu
> 
> **Dòng 10-12**: Đẩy dữ liệu vào đối tượng Model. Model này sẽ được Thymeleaf sử dụng để render HTML
> 
> **Dòng 14**: Trả về tên template `shoe/shoes-list`, tương ứng với file `shoes-list.html`"

---

# 🎬 SLIDE 6: CODE - SERVICE (View Shoe List)
## ⏱️ Thời gian: 2 phút

### 📺 Nội dung trên slide:

**📁 File: `ShoesService.java`**

```java
@Transactional(readOnly = true)
public ShoesListDto getShoesList(int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);

    // Query 1: Lấy danh sách có phân trang
    Page<Shoes> shoesPage = shoesRepository.findAllPaged(pageable);

    // Lấy danh sách ID
    List<Long> shoeIds = new ArrayList<>();
    for (Shoes s : shoesPage.getContent()) {
        shoeIds.add(s.getShoeId());
    }

    // Query 2: Lấy chi tiết kèm images
    List<Shoes> shoesList = shoesRepository.findAllByIdsWithImages(shoeIds);

    // Chuyển đổi sang DTO
    List<ShoesSummaryDto> products = new ArrayList<>();
    for (Shoes shoes : shoesList) {
        products.add(convertToSummaryDto(shoes));
    }

    return ShoesListDto.builder()...build();
}
```

### 🎤 Lời nói:
> "Đây là code của Service, nơi chứa business logic chính.
> 
> **Dòng 1**: Annotation `@Transactional(readOnly = true)` giúp tối ưu performance vì đây là thao tác chỉ đọc, không ghi dữ liệu.
> 
> **Dòng 3**: Tạo đối tượng Pageable. Lưu ý Spring Data dùng index bắt đầu từ 0, nên phải trừ 1.
> 
> **Dòng 6**: **Query 1** - Gọi `findAllPaged()` để lấy danh sách sản phẩm có phân trang
> 
> **Dòng 9-12**: Lấy danh sách ID từ kết quả query 1
> 
> **Dòng 15**: **Query 2** - Gọi `findAllByIdsWithImages()` để lấy chi tiết sản phẩm kèm hình ảnh
> 
> **Tại sao tách thành 2 query?** Vì nếu dùng 1 query với JOIN FETCH, sẽ không thể kết hợp với phân trang được. Đây là cách tối ưu để tránh N+1 problem.
> 
> **Dòng 18-21**: Chuyển đổi từ Entity sang DTO bằng method `convertToSummaryDto()`"

---

# 🎬 SLIDE 7: CODE - REPOSITORY (View Shoe List)
## ⏱️ Thời gian: 1 phút

### 📺 Nội dung trên slide:

**📁 File: `ShoesRepository.java`**

```java
@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    // Query 1: Lấy danh sách có phân trang
    @Query("SELECT s FROM Shoes s")
    Page<Shoes> findAllPaged(Pageable pageable);

    // Query 2: Lấy chi tiết theo IDs kèm images
    @Query("SELECT DISTINCT s FROM Shoes s "
            + "LEFT JOIN FETCH s.images "
            + "WHERE s.shoeId IN :ids")
    List<Shoes> findAllByIdsWithImages(@Param("ids") List<Long> ids);
}
```

### 🎤 Lời nói:
> "Đây là code của Repository, interface để truy vấn database.
> 
> Repository kế thừa từ `JpaRepository` nên có sẵn các method CRUD cơ bản như save, delete, findById.
> 
> **Method findAllPaged**: Query đơn giản, Spring Data JPA sẽ tự động xử lý phân trang dựa trên tham số Pageable
> 
> **Method findAllByIdsWithImages**: 
> - `LEFT JOIN FETCH s.images`: Đây là kỹ thuật **Eager Loading**, tải hình ảnh cùng lúc với sản phẩm trong 1 query, tránh Lazy Loading Exception
> - `DISTINCT`: Tránh duplicate khi JOIN với collection
> - `WHERE s.shoeId IN :ids`: Chỉ lấy những sản phẩm có ID trong danh sách"

---

# 🎬 SLIDE 8: SEQUENCE DIAGRAM - VIEW SHOE DETAIL
## ⏱️ Thời gian: 1 phút 30 giây

### 📺 Nội dung trên slide:
*(Chèn hình Sequence Diagram từ file `03_ViewShoeDetail_Sequence.puml`)*

### 🎤 Lời nói:
> "Tiếp theo là Sequence Diagram của chức năng View Shoe Detail.
> 
> **Bước 1**: Từ trang danh sách, người dùng click vào một sản phẩm
> 
> **Bước 2**: Request `GET /product/{id}` được gửi đến Controller
> 
> **Bước 3**: Service thực hiện **3 query** đến database:
> - **Query 1**: `findByIdWithImages()` - Lấy sản phẩm kèm hình ảnh và category
> - **Query 2**: `findByIdWithVariants()` - Lấy variants (size/color) riêng
> - **Query 3**: `findRelatedProducts()` - Lấy sản phẩm liên quan
> 
> **Tại sao tách Query 1 và Query 2?** Vì nếu JOIN FETCH cả images và variants trong 1 query sẽ xảy ra **tích Descartes**: 5 images × 10 variants = 50 rows thay vì 15 rows.
> 
> **Bước 4**: Dữ liệu được merge, chuyển đổi sang DTO và hiển thị"

---

# 🎬 SLIDE 9: CODE - CONTROLLER (View Shoe Detail)
## ⏱️ Thời gian: 1 phút 30 giây

### 📺 Nội dung trên slide:

**📁 File: `ShoesController.java`**

```java
@GetMapping("/product/{shoeId}")
public String productDetail(@PathVariable Long shoeId, Model model) {
    
    // 1. Lấy thông tin chi tiết sản phẩm
    ShoesDetailDto product = shoesService.getShoesDetail(shoeId);
    model.addAttribute("product", product);

    // 2. Lấy danh sách đánh giá
    List<Review> reviews = reviewRepository.findByShoesIdWithDetails(shoeId);
    model.addAttribute("reviews", reviews);

    // 3. Tính rating trung bình
    double averageRate = reviews.stream()
            .mapToInt(Review::getRate)
            .average()
            .orElse(0.0);
    model.addAttribute("averageRate", averageRate);

    return "shoe/shoes-detail";
}
```

### 🎤 Lời nói:
> "Đây là method `productDetail()` trong Controller.
> 
> **Dòng 1**: URL pattern `/product/{shoeId}` với `{shoeId}` là path variable. Ví dụ `/product/5` thì shoeId = 5
> 
> **Dòng 2**: Annotation `@PathVariable` cho phép lấy giá trị từ URL path
> 
> **Dòng 5-6**: Gọi Service lấy thông tin chi tiết sản phẩm
> 
> **Dòng 9-10**: Lấy danh sách đánh giá từ ReviewRepository
> 
> **Dòng 13-17**: Tính điểm đánh giá trung bình bằng **Stream API**. Method `stream()` chuyển List thành Stream, `mapToInt()` lấy ra điểm rate, `average()` tính trung bình, `orElse(0.0)` trả về 0 nếu không có đánh giá nào
> 
> Ngoài ra trong code thực tế còn lấy thêm các chương trình khuyến mãi đang áp dụng."

---

# 🎬 SLIDE 10: CODE - SERVICE (View Shoe Detail)
## ⏱️ Thời gian: 2 phút

### 📺 Nội dung trên slide:

**📁 File: `ShoesService.java`**

```java
@Transactional(readOnly = true)
public ShoesDetailDto getShoesDetail(Long shoeId) {
    
    // Query 1: Lấy shoes với images và category
    Shoes shoes = shoesRepository.findByIdWithImages(shoeId)
            .orElseThrow(() -> new NotFoundException(
                "Không tìm thấy sản phẩm ID: " + shoeId));

    // Query 2: Lấy variants riêng (tránh tích Descartes)
    Shoes shoesWithVariants = shoesRepository.findByIdWithVariants(shoeId)
            .orElse(shoes);
    
    // Merge variants vào shoes entity
    shoes.setVariants(shoesWithVariants.getVariants());

    // Chuyển đổi sang DTO (bao gồm lấy related products)
    return convertToDetailDto(shoes);
}
```

### 🎤 Lời nói:
> "Đây là method `getShoesDetail()` trong Service.
> 
> **Dòng 5-7**: **Query 1** - Lấy sản phẩm kèm images và category. Method `orElseThrow()` sẽ ném exception nếu không tìm thấy sản phẩm
> 
> **Dòng 10-11**: **Query 2** - Lấy variants riêng biệt
> 
> **Tại sao phải tách thành 2 query?** 
> 
> Nếu em JOIN FETCH cả images và variants trong 1 query, Hibernate sẽ tạo ra **tích Descartes**. Ví dụ: sản phẩm có 5 hình ảnh và 10 variants, kết quả query sẽ trả về 5 × 10 = 50 rows thay vì 5 + 10 = 15 rows. Điều này gây lãng phí bộ nhớ và làm chậm hệ thống.
> 
> **Dòng 14**: Merge variants từ query 2 vào entity shoes
> 
> **Dòng 17**: Gọi `convertToDetailDto()` để chuyển đổi sang DTO. Trong method này cũng sẽ gọi thêm **Query 3** để lấy sản phẩm liên quan."

---

# 🎬 SLIDE 11: TỔNG HỢP CÁC FILE CODE
## ⏱️ Thời gian: 1 phút

### 📺 Nội dung trên slide:

| Layer | File | Mô tả |
|-------|------|-------|
| **Entity** | `Shoes.java` | Entity chính, mapping bảng `shoes` |
| **Entity** | `ShoesImage.java` | Entity hình ảnh sản phẩm |
| **Entity** | `ShoesVariant.java` | Entity biến thể (size/color) |
| **Controller** | `ShoesController.java` | Xử lý HTTP request |
| **Service** | `ShoesService.java` | Business logic |
| **Repository** | `ShoesRepository.java` | Truy vấn database |
| **DTO** | `ShoesListDto.java` | DTO cho danh sách |
| **DTO** | `ShoesSummaryDto.java` | DTO cho card sản phẩm |
| **DTO** | `ShoesDetailDto.java` | DTO cho chi tiết |
| **Template** | `shoes-list.html` | Giao diện danh sách |
| **Template** | `shoes-detail.html` | Giao diện chi tiết |

### 🎤 Lời nói:
> "Đây là tổng hợp tất cả các file code liên quan đến 2 chức năng.
> 
> Ở **Entity Layer** có 3 file: Shoes là entity chính, ShoesImage cho hình ảnh, ShoesVariant cho các biến thể size/color
> 
> **Controller, Service, Repository** mỗi layer có 1 file chính
> 
> Có 3 file **DTO** để chuyển đổi dữ liệu: ShoesListDto chứa danh sách và thông tin phân trang, ShoesSummaryDto cho mỗi card sản phẩm, ShoesDetailDto cho trang chi tiết
> 
> Và 2 file **Template** Thymeleaf để hiển thị giao diện"

---

# 🎬 SLIDE 12: KẾT LUẬN & HỎI ĐÁP
## ⏱️ Thời gian: 1 phút

### 📺 Nội dung trên slide:

**📊 Thống kê Database Query:**

| Chức năng | Số Query |
|-----------|----------|
| View Shoe List | 2 queries |
| View Shoe Detail | 3 queries |

**🎯 Kỹ thuật đã sử dụng:**
- ✅ JOIN FETCH - Tránh N+1 problem
- ✅ Tách query - Tránh tích Descartes  
- ✅ DTO Pattern - Tách biệt Entity và View
- ✅ Builder Pattern - Tạo object dễ đọc
- ✅ Pagination - Phân trang dữ liệu

**❓ Hỏi đáp**

### 🎤 Lời nói:
> "Tóm lại, 2 chức năng View Shoe List và View Shoe Detail đã được implement theo đúng kiến trúc MVC và Layered Architecture.
> 
> Em đã áp dụng các kỹ thuật tối ưu như:
> - **JOIN FETCH** để tránh N+1 query problem
> - **Tách query** khi fetch nhiều collection để tránh tích Descartes
> - **DTO Pattern** để tách biệt dữ liệu hiển thị với entity database
> - **Builder Pattern** để code dễ đọc và maintain hơn
> 
> Tổng cộng View Shoe List chỉ cần 2 query, View Shoe Detail cần 3 query, đảm bảo performance tốt.
> 
> Cảm ơn thầy/cô và các bạn đã lắng nghe. Em xin sẵn sàng trả lời các câu hỏi ạ."

---

# 📎 PHỤ LỤC: CÂU HỎI CÓ THỂ ĐƯỢC HỎI

## ❓ Câu hỏi 1: Tại sao dùng DTO thay vì trả trực tiếp Entity?

### 💡 Trả lời:
> "Có 3 lý do chính:
> 1. **Bảo mật**: Entity có thể chứa các field nhạy cảm không muốn expose ra ngoài
> 2. **Tối ưu**: DTO chỉ chứa những field cần thiết, giảm dữ liệu truyền tải
> 3. **Độc lập**: Thay đổi Entity không ảnh hưởng đến API/View, và ngược lại"

---

## ❓ Câu hỏi 2: N+1 problem là gì?

### 💡 Trả lời:
> "N+1 problem xảy ra khi:
> - 1 query lấy danh sách N sản phẩm
> - N query khác để lấy hình ảnh của từng sản phẩm
> - Tổng cộng N+1 queries
> 
> Ví dụ: 12 sản phẩm = 1 + 12 = 13 queries
> 
> **Cách giải quyết**: Dùng JOIN FETCH để lấy tất cả trong 1-2 query"

---

## ❓ Câu hỏi 3: Tích Descartes là gì?

### 💡 Trả lời:
> "Khi JOIN FETCH nhiều collection trong 1 query, Hibernate sẽ tạo tích Descartes.
> 
> Ví dụ: Sản phẩm có 5 images và 10 variants
> - Mong đợi: 5 + 10 = 15 rows
> - Thực tế: 5 × 10 = 50 rows (duplicate)
> 
> **Cách giải quyết**: Tách thành 2 query riêng biệt"

---

## ❓ Câu hỏi 4: Tại sao dùng @Transactional(readOnly = true)?

### 💡 Trả lời:
> "Annotation này cho Hibernate biết đây là transaction chỉ đọc, không ghi dữ liệu.
> 
> **Lợi ích**:
> - Hibernate không cần track dirty entities
> - Không cần flush changes cuối transaction
> - Tối ưu performance cho các thao tác đọc"

---

## ❓ Câu hỏi 5: Builder Pattern là gì?

### 💡 Trả lời:
> "Builder Pattern là design pattern giúp tạo object với nhiều field một cách dễ đọc.
> 
> Thay vì constructor dài với nhiều tham số:
> ```java
> new ShoesDetailDto(id, name, brand, price, ...)  // Khó đọc
> ```
> 
> Dùng Builder:
> ```java
> ShoesDetailDto.builder()
>     .shoeId(1L)
>     .name("Nike Air")
>     .brand("Nike")
>     .build();  // Dễ đọc hơn
> ```
> 
> Trong project em dùng Lombok `@Builder` để tự động generate code"

---

**📝 Tác giả:** [Tên sinh viên]  
**📅 Ngày tạo:** 31/12/2025
