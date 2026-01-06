# 🎯 BÀI THUYẾT TRÌNH: CHỨC NĂNG VIEW SHOE LIST & VIEW SHOE DETAIL

---

## 📑 MỤC LỤC

1. [Giới thiệu chức năng](#1-giới-thiệu-chức-năng)
2. [Kiến trúc tổng quan](#2-kiến-trúc-tổng-quan)
3. [Chức năng 1: View Shoe List](#3-chức-năng-1-view-shoe-list)
4. [Chức năng 2: View Shoe Detail](#4-chức-năng-2-view-shoe-detail)
5. [Các file code liên quan](#5-các-file-code-liên-quan)
6. [Tổng kết](#6-tổng-kết)

---

## 1. GIỚI THIỆU CHỨC NĂNG

### 1.1. View Shoe List (Xem danh sách giày)

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Hiển thị danh sách sản phẩm giày trên trang chủ |
| **Actor** | Registered Customer / Unregistered Customer |
| **Endpoint** | `GET /` hoặc `GET /?page=1&size=12` |
| **Template** | `shoe/shoes-list.html` |

**Tính năng chính:**
- ✅ Hiển thị danh sách sản phẩm dạng card (thumbnail, tên, giá, brand)
- ✅ Phân trang (mặc định 12 sản phẩm/trang)
- ✅ Hiển thị danh mục và thương hiệu để filter

---

### 1.2. View Shoe Detail (Xem chi tiết giày)

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Hiển thị thông tin chi tiết của 1 sản phẩm giày |
| **Actor** | Registered Customer / Unregistered Customer |
| **Endpoint** | `GET /product/{shoeId}` |
| **Template** | `shoe/shoes-detail.html` |

**Tính năng chính:**
- ✅ Hiển thị gallery hình ảnh sản phẩm
- ✅ Thông tin: tên, giá, mô tả, thương hiệu, danh mục
- ✅ Chọn size và màu sắc (variants)
- ✅ Hiển thị tồn kho
- ✅ Danh sách đánh giá (reviews) và rating trung bình
- ✅ Sản phẩm liên quan (cùng category)
- ✅ Khuyến mãi đang áp dụng

---

## 2. KIẾN TRÚC TỔNG QUAN

### 2.1. Mô hình MVC (Model - View - Controller)

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │  shoes-list.html│    │shoes-detail.html│   (Thymeleaf)       │
│  └────────┬────────┘    └────────┬────────┘                     │
└───────────┼──────────────────────┼──────────────────────────────┘
            │                      │
            ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CONTROLLER LAYER                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    ShoesController.java                  │    │
│  │  • GET /              → homePage()                       │    │
│  │  • GET /product/{id}  → productDetail()                  │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                         SERVICE LAYER                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    ShoesService.java                     │    │
│  │  • getShoesList(page, size)    → ShoesListDto            │    │
│  │  • getShoesDetail(shoeId)      → ShoesDetailDto          │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        REPOSITORY LAYER                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   ShoesRepository.java                   │    │
│  │  • findAllPaged(pageable)                                │    │
│  │  • findAllByIdsWithImages(ids)                           │    │
│  │  • findByIdWithImages(shoeId)                            │    │
│  │  • findByIdWithVariants(shoeId)                          │    │
│  │  • findRelatedProducts(categoryId, excludeId, pageable)  │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                          DATABASE                                │
│  ┌─────────┐  ┌─────────────┐  ┌───────────────┐  ┌──────────┐  │
│  │  shoes  │  │ shoes_image │  │ shoes_variant │  │ category │  │
│  └─────────┘  └─────────────┘  └───────────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. CHỨC NĂNG 1: VIEW SHOE LIST

### 3.1. Luồng hoạt động (Sequence Flow)

```
User → ShoeListUI → ShoesController → ShoesService → ShoesRepository → Database
```

**Các bước chi tiết:**

| Bước | Mô tả | Code |
|------|-------|------|
| 1 | User truy cập trang chủ | `GET /` hoặc `GET /?page=1&size=12` |
| 2 | Controller nhận request | `ShoesController.homePage()` |
| 3 | Gọi Service lấy data | `shoesService.getShoesList(page, size)` |
| 4 | Query danh sách có phân trang | `shoesRepository.findAllPaged(pageable)` |
| 5 | Query images cho các shoes | `shoesRepository.findAllByIdsWithImages(ids)` |
| 6 | Convert Entity → DTO | `convertToSummaryDto(shoes)` |
| 7 | Trả về ShoesListDto | Chứa List<ShoesSummaryDto> + pagination info |
| 8 | Add vào Model | `model.addAttribute("products", ...)` |
| 9 | Render template | Return `"shoe/shoes-list"` |

---

### 3.2. Chi tiết từng file code

#### 📄 **File 1: ShoesController.java** (Controller Layer)

**Đường dẫn:** `src/main/java/ecommerce/shoestore/shoes/ShoesController.java`

```java
@GetMapping("/")
public String homePage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "12") int size,
        Model model) {
    
    // Gọi Service để lấy danh sách sản phẩm
    ShoesListDto data = shoesService.getShoesList(page, size);

    // Đẩy dữ liệu vào Model để Thymeleaf render
    model.addAttribute("products", data.getProducts());
    model.addAttribute("currentPage", data.getCurrentPage());
    model.addAttribute("totalPages", data.getTotalPages());
    model.addAttribute("totalItems", data.getTotalItems());
    model.addAttribute("categories", categoryRepository.findAll());
    model.addAttribute("brands", shoesSearchService.findAllBrands(null));

    return "shoe/shoes-list";
}
```

**Giải thích:**
- `@GetMapping("/")`: Xử lý request GET đến trang chủ
- `@RequestParam(defaultValue = "1")`: Tham số page mặc định là 1
- `Model model`: Đối tượng để truyền dữ liệu sang View

---

#### 📄 **File 2: ShoesService.java** (Service Layer)

**Đường dẫn:** `src/main/java/ecommerce/shoestore/shoes/ShoesService.java`

```java
@Transactional(readOnly = true)
public ShoesListDto getShoesList(int page, int size) {
    // Tạo Pageable (page - 1 vì Spring Data dùng 0-based index)
    Pageable pageable = PageRequest.of(page - 1, size);

    // Bước 1: Lấy danh sách ID (có phân trang)
    Page<Shoes> shoesPage = shoesRepository.findAllPaged(pageable);

    // Bước 2: Lấy danh sách ID từ kết quả
    List<Long> shoeIds = new ArrayList<>();
    for (Shoes s : shoesPage.getContent()) {
        shoeIds.add(s.getShoeId());
    }

    // Bước 3: Lấy chi tiết giày theo IDs (kèm images)
    List<Shoes> shoesList = new ArrayList<>();
    if (!shoeIds.isEmpty()) {
        shoesList = shoesRepository.findAllByIdsWithImages(shoeIds);
    }

    // Bước 4: Chuyển đổi sang DTO
    List<ShoesSummaryDto> products = new ArrayList<>();
    for (Shoes shoes : shoesList) {
        products.add(convertToSummaryDto(shoes));
    }

    // Bước 5: Build và trả về ShoesListDto
    return ShoesListDto.builder()
            .products(products)
            .currentPage(page)
            .totalPages(shoesPage.getTotalPages())
            .totalItems(shoesPage.getTotalElements())
            .build();
}
```

**Giải thích:**
- `@Transactional(readOnly = true)`: Tối ưu performance cho query chỉ đọc
- Tách thành 2 query để tránh N+1 problem
- Sử dụng Builder pattern để tạo DTO

---

#### 📄 **File 3: ShoesRepository.java** (Repository Layer)

**Đường dẫn:** `src/main/java/ecommerce/shoestore/shoes/ShoesRepository.java`

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

**Giải thích:**
- `LEFT JOIN FETCH s.images`: Eager loading để tránh Lazy Loading Exception
- `DISTINCT`: Tránh duplicate khi JOIN với collection

---

#### 📄 **File 4: DTOs (Data Transfer Objects)**

**ShoesSummaryDto.java** - DTO cho mỗi card sản phẩm:
```java
public class ShoesSummaryDto {
    private Long shoeId;
    private String name;
    private String brand;
    private BigDecimal price;
    private String thumbnailUrl;
    private boolean outOfStock;
    private boolean isNew;
    private String type;
}
```

**ShoesListDto.java** - DTO chứa danh sách + phân trang:
```java
public class ShoesListDto {
    private List<ShoesSummaryDto> products;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
```

---

## 4. CHỨC NĂNG 2: VIEW SHOE DETAIL

### 4.1. Luồng hoạt động (Sequence Flow)

```
User → ShoeDetailUI → ShoesController → ShoesService → ShoesRepository → Database
```

**Các bước chi tiết:**

| Bước | Mô tả | Code |
|------|-------|------|
| 1 | User click vào sản phẩm | `GET /product/{shoeId}` |
| 2 | Controller nhận request | `ShoesController.productDetail()` |
| 3 | Gọi Service lấy chi tiết | `shoesService.getShoesDetail(shoeId)` |
| 4 | Query shoes + images + category | `shoesRepository.findByIdWithImages(shoeId)` |
| 5 | Query variants riêng | `shoesRepository.findByIdWithVariants(shoeId)` |
| 6 | Merge variants vào shoes | `shoes.setVariants(...)` |
| 7 | Lấy sản phẩm liên quan | `getRelatedProducts(shoes)` |
| 8 | Convert Entity → DTO | `convertToDetailDto(shoes)` |
| 9 | Lấy thêm reviews, campaigns | Query từ các repository khác |
| 10 | Render template | Return `"shoe/shoes-detail"` |

---

### 4.2. Chi tiết từng file code

#### 📄 **File 1: ShoesController.java** (Controller Layer)

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
    
    // 4. Lấy các campaign khuyến mãi đang áp dụng
    List<PromotionCampaign> activeCampaigns = customerPromotionService
            .getActiveCampaignsForProduct(shoeId, product.getCategoryId());
    model.addAttribute("activeCampaigns", activeCampaigns);

    return "shoe/shoes-detail";
}
```

**Giải thích:**
- `@PathVariable Long shoeId`: Lấy ID từ URL path
- Ngoài thông tin sản phẩm, còn lấy thêm: reviews, rating, khuyến mãi

---

#### 📄 **File 2: ShoesService.java** (Service Layer)

```java
@Transactional(readOnly = true)
public ShoesDetailDto getShoesDetail(Long shoeId) {
    
    // Query 1: Lấy shoes với images và category
    Shoes shoes = shoesRepository.findByIdWithImages(shoeId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

    // Query 2: Lấy thêm variants (query riêng để tránh tích Descartes)
    Shoes shoesWithVariants = shoesRepository.findByIdWithVariants(shoeId)
            .orElse(shoes);
    
    // Merge variants vào shoes entity
    shoes.setVariants(shoesWithVariants.getVariants());

    // Convert sang DTO và trả về
    return convertToDetailDto(shoes);
}
```

**Tại sao tách thành 2 query?**
- Nếu JOIN FETCH cả `images` và `variants` trong 1 query → Tích Descartes
- Ví dụ: 5 images × 10 variants = 50 rows → duplicate data

---

#### 📄 **File 3: Method convertToDetailDto()**

```java
private ShoesDetailDto convertToDetailDto(Shoes shoes) {
    // 1. Lấy tên danh mục
    String categoryName = "General";
    if (shoes.getCategory() != null) {
        categoryName = shoes.getCategory().getName();
    }

    // 2. Lấy danh sách URL hình ảnh
    List<String> imageUrls = new ArrayList<>();
    if (shoes.getImages() != null) {
        for (ShoesImage img : shoes.getImages()) {
            imageUrls.add(img.getUrl());
        }
    }

    // 3. Xử lý variants: lấy sizes, colors, tính tổng stock
    Set<String> sizes = new HashSet<>();
    Set<String> colors = new HashSet<>();
    int totalStock = 0;
    List<ShoesVariantDto> variants = new ArrayList<>();

    if (shoes.getVariants() != null) {
        for (ShoesVariant v : shoes.getVariants()) {
            sizes.add(v.getSizeValue());
            colors.add(v.getColorValue());
            totalStock += v.getStock();
            variants.add(ShoesVariantDto.builder()
                    .variantId(v.getVariantId())
                    .size(v.getSizeValue())
                    .color(v.getColorValue())
                    .stock(v.getStock())
                    .build());
        }
    }

    // 4. Lấy sản phẩm liên quan
    List<ShoesSummaryDto> relatedProducts = getRelatedProducts(shoes);

    // 5. Build DTO
    return ShoesDetailDto.builder()
            .shoeId(shoes.getShoeId())
            .name(shoes.getName())
            .brand(shoes.getBrand())
            .basePrice(shoes.getBasePrice())
            .description(shoes.getDescription())
            .category(categoryName)
            .imageUrls(imageUrls)
            .sizes(sizes)
            .colors(colors)
            .variants(variants)
            .totalStock(totalStock)
            .relatedProducts(relatedProducts)
            .build();
}
```

---

#### 📄 **File 4: ShoesRepository.java** (Repository Layer)

```java
// Query 1: Lấy shoes với images và category
@Query("SELECT s FROM Shoes s " +
       "LEFT JOIN FETCH s.category " +
       "LEFT JOIN FETCH s.images " +
       "WHERE s.shoeId = :shoeId")
Optional<Shoes> findByIdWithImages(@Param("shoeId") Long shoeId);

// Query 2: Lấy variants riêng
@Query("SELECT s FROM Shoes s " +
       "LEFT JOIN FETCH s.variants " +
       "WHERE s.shoeId = :shoeId")
Optional<Shoes> findByIdWithVariants(@Param("shoeId") Long shoeId);

// Query 3: Lấy sản phẩm liên quan (cùng category)
@Query("SELECT DISTINCT s FROM Shoes s "
        + "LEFT JOIN FETCH s.images "
        + "WHERE s.category.categoryId = :categoryId "
        + "AND s.shoeId != :excludeShoeId "
        + "AND s.status = true")
List<Shoes> findRelatedProducts(
        @Param("categoryId") Long categoryId,
        @Param("excludeShoeId") Long excludeShoeId,
        Pageable pageable);
```

---

#### 📄 **File 5: ShoesDetailDto.java**

```java
public class ShoesDetailDto {
    private Long shoeId;
    private String name;
    private String brand;
    private BigDecimal basePrice;
    private String description;
    private String category;
    private Long categoryId;
    private Set<String> sizes;          // Các size có sẵn
    private Set<String> colors;         // Các màu có sẵn
    private String collection;
    private String type;
    private List<String> imageUrls;     // Gallery hình ảnh
    private Integer totalStock;         // Tổng tồn kho
    private List<ShoesVariantDto> variants;        // Chi tiết từng variant
    private List<ShoesSummaryDto> relatedProducts; // Sản phẩm liên quan
}
```

---

## 5. CÁC FILE CODE LIÊN QUAN

### 5.1. Bảng tổng hợp các file

| Layer | File | Mô tả |
|-------|------|-------|
| **Entity** | `Shoes.java` | Entity chính cho bảng `shoes` |
| **Entity** | `ShoesImage.java` | Entity cho hình ảnh sản phẩm |
| **Entity** | `ShoesVariant.java` | Entity cho biến thể (size/color) |
| **Controller** | `ShoesController.java` | Xử lý HTTP request |
| **Service** | `ShoesService.java` | Business logic |
| **Repository** | `ShoesRepository.java` | Truy vấn database |
| **DTO** | `ShoesListDto.java` | DTO cho danh sách + phân trang |
| **DTO** | `ShoesSummaryDto.java` | DTO cho card sản phẩm |
| **DTO** | `ShoesDetailDto.java` | DTO cho trang chi tiết |
| **Template** | `shoes-list.html` | Giao diện danh sách |
| **Template** | `shoes-detail.html` | Giao diện chi tiết |

---

### 5.2. Cấu trúc thư mục

```
src/main/java/ecommerce/shoestore/
├── shoes/
│   ├── Shoes.java                 ← Entity
│   ├── ShoesType.java             ← Enum (MEN, WOMEN, KIDS, UNISEX)
│   ├── ShoesController.java       ← Controller
│   ├── ShoesService.java          ← Service
│   ├── ShoesRepository.java       ← Repository
│   └── dto/
│       ├── ShoesListDto.java      ← DTO danh sách
│       ├── ShoesSummaryDto.java   ← DTO tóm tắt
│       └── ShoesDetailDto.java    ← DTO chi tiết
├── shoesimage/
│   └── ShoesImage.java            ← Entity hình ảnh
├── shoesvariant/
│   ├── ShoesVariant.java          ← Entity variant
│   └── ShoesVariantDto.java       ← DTO variant
└── category/
    └── Category.java              ← Entity danh mục
```

---

## 6. TỔNG KẾT

### 6.1. Số lượng Database Query

| Chức năng | Số Query | Chi tiết |
|-----------|----------|----------|
| **View Shoe List** | 2 queries | 1. `findAllPaged()` 2. `findAllByIdsWithImages()` |
| **View Shoe Detail** | 3 queries | 1. `findByIdWithImages()` 2. `findByIdWithVariants()` 3. `findRelatedProducts()` |

### 6.2. Design Patterns sử dụng

| Pattern | Mô tả |
|---------|-------|
| **MVC** | Model-View-Controller architecture |
| **DTO** | Data Transfer Object - tách biệt Entity và View |
| **Repository** | Abstraction layer cho database access |
| **Builder** | Sử dụng Lombok @Builder để tạo DTO |
| **Dependency Injection** | Sử dụng @RequiredArgsConstructor |

### 6.3. Kỹ thuật tối ưu

| Kỹ thuật | Mục đích |
|----------|----------|
| **JOIN FETCH** | Tránh N+1 problem, eager loading |
| **Tách query** | Tránh tích Descartes khi fetch nhiều collection |
| **@Transactional(readOnly)** | Tối ưu performance cho query chỉ đọc |
| **Pagination** | Giới hạn số record trả về |

---

## 📊 SEQUENCE DIAGRAMS

Xem chi tiết tại:
- `diagrams/01_ViewShoeList_Sequence.puml`
- `diagrams/03_ViewShoeDetail_Sequence.puml`

---

**📝 Tác giả:** [Tên sinh viên]  
**📅 Ngày:** 31/12/2025  
**🏫 Môn học:** Software Engineering Project
