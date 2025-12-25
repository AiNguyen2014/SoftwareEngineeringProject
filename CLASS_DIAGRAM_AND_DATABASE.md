# Class Diagram & Database Schema - Webshoe E-commerce

## 📋 Mục lục
1. [Class Diagram - Quản lý Sản phẩm](#1-class-diagram---quản-lý-sản-phẩm)
2. [Class Diagram - Quản lý Khuyến mãi](#2-class-diagram---quản-lý-khuyến-mãi)
3. [Mối quan hệ giữa Product và Promotion](#3-mối-quan-hệ-giữa-product-và-promotion)
4. [Khi nào hiển thị thuộc tính trong Class Diagram?](#4-khi-nào-hiển-thị-thuộc-tính-trong-class-diagram)
5. [Database Schema Chi tiết](#5-database-schema-chi-tiết)

---

## 1. Class Diagram - Quản lý Sản phẩm

```
┌─────────────────────────────────────┐
│          Category                   │
├─────────────────────────────────────┤
│ - categoryId: Long (PK)             │
│ - name: String (Unique, NotNull)    │
├─────────────────────────────────────┤
│ + getters/setters                   │
└─────────────────────────────────────┘
            △
            │ 1
            │
            │ *
┌─────────────────────────────────────┐
│          Shoes                      │
├─────────────────────────────────────┤
│ - shoeId: Long (PK)                 │
│ - name: String (NotNull)            │
│ - brand: String                     │
│ - type: ShoesType (Enum)            │
│ - basePrice: BigDecimal (NotNull)   │
│ - description: String (Text)        │
│ - collection: String                │
│ - status: Boolean (Default: true)   │
│ - createdAt: LocalDateTime          │
├─────────────────────────────────────┤
│ + getters/setters                   │
└─────────────────────────────────────┘
       △                   △
       │ 1                 │ 1
       │                   │
       │ *                 │ *
┌──────────────┐    ┌─────────────────┐
│  ShoesImage  │    │  ShoesVariant   │
├──────────────┤    ├─────────────────┤
│ - imageId    │    │ - variantId     │
│ - url        │    │ - size: Size    │
│ - isThumbnail│    │ - color: Color  │
│ (Boolean)    │    │ - stock: Integer│
└──────────────┘    └─────────────────┘

<<enumeration>>         <<enumeration>>
 ShoesType               Size
─────────────           ─────────────
 FOR_MALE                SIZE_35
 FOR_FEMALE              SIZE_36
 FOR_UNISEX              ...
                         SIZE_45

<<enumeration>>
 Color
─────────────
 BLACK
 WHITE
 RED
 GRAY
 BROWN
 PINK
 BLUE
 GREEN
```

### Quan hệ:
- **Category → Shoes**: 1-to-Many (Một danh mục có nhiều sản phẩm)
- **Shoes → ShoesImage**: 1-to-Many, Cascade ALL, Orphan Removal (Một sản phẩm có nhiều ảnh)
- **Shoes → ShoesVariant**: 1-to-Many, Cascade ALL, Orphan Removal (Một sản phẩm có nhiều biến thể)

---

## 2. Class Diagram - Quản lý Khuyến mãi

```
┌─────────────────────────────────────────────────┐
│          PromotionCampaign                      │
├─────────────────────────────────────────────────┤
│ - campaignId: Long (PK)                         │
│ - name: String (NotNull)                        │
│ - description: String                           │
│ - startDate: LocalDate (NotNull)                │
│ - endDate: LocalDate (NotNull)                  │
│ - discountType: VoucherDiscountType (Enum)      │
│ - discountValue: BigDecimal (NotNull)           │
│ - maxDiscountAmount: BigDecimal                 │
│ - minOrderValue: BigDecimal                     │
│ - status: PromotionCampaignStatus (Enum)        │
│ - enabled: Boolean (NotNull)                    │
├─────────────────────────────────────────────────┤
│ + getters/setters                               │
└─────────────────────────────────────────────────┘
        △                          △
        │ 1                        │ 1
        │                          │
        │ *                        │ *
┌──────────────────┐    ┌──────────────────────────┐
│    Voucher       │    │   PromotionTarget        │
├──────────────────┤    ├──────────────────────────┤
│ - voucherId      │    │ - targetId               │
│ - code (Unique)  │    │ - targetType (Enum)      │
│ - title          │    │   • ALL                  │
│ - discountType   │    │   • PRODUCT              │
│ - discountValue  │    │   • CATEGORY             │
│ - startDate      │    └──────────────────────────┘
│ - endDate        │              │ *  │ *
│ - enabled        │              │    │
└──────────────────┘              │    │
        │                         │    │
        │ 1                       │    │
        │                    ┌────┘    └────┐
        │ *                  │              │
┌──────────────────┐         │              │
│  OrderVoucher    │   ┌─────▽──────┐ ┌────▽─────┐
├──────────────────┤   │   Shoes    │ │ Category │
│ - orderVoucherId │   │  (Product) │ │          │
│ - orderId (FK)   │   └────────────┘ └──────────┘
│ - userId         │
│ - discountAmount │
│ - createdAt      │
└──────────────────┘

<<enumeration>>              <<enumeration>>              <<enumeration>>
VoucherDiscountType      PromotionCampaignStatus     ProductTargetType
───────────────────      ───────────────────────     ─────────────────
 PERCENTAGE               DRAFT                        ALL
 FIXED_AMOUNT             ACTIVE                       PRODUCT
                          ENDED                        CATEGORY
                          CANCELLED
```

### Quan hệ:
- **PromotionCampaign → Voucher**: 1-to-Many, Cascade ALL, Orphan Removal
- **PromotionCampaign → PromotionTarget**: 1-to-Many, Cascade ALL, Orphan Removal
- **Voucher → OrderVoucher**: 1-to-Many (liên kết với Order khi customer sử dụng)
- **PromotionTarget → Shoes**: Many-to-One (Nullable - chỉ khi targetType = PRODUCT)
- **PromotionTarget → Category**: Many-to-One (Nullable - chỉ khi targetType = CATEGORY)

---

## 3. Mối quan hệ giữa Product và Promotion

```
┌────────────────┐
│    Shoes       │
│  (Product)     │
└────────────────┘
        △
        │
        │ * (Optional)
        │
┌───────────────────────┐        1        ┌──────────────────────┐
│  PromotionTarget      │◄────────────────│  PromotionCampaign   │
├───────────────────────┤                 ├──────────────────────┤
│ - targetType          │                 │ - discountType       │
│   • ALL               │                 │ - discountValue      │
│   • PRODUCT (FK)      │                 │ - status             │
│   • CATEGORY (FK)     │                 │ - enabled            │
└───────────────────────┘                 └──────────────────────┘
        │                                          △
        │ * (Optional)                             │ 1
        │                                          │
        ▼                                          │ *
┌────────────────┐                         ┌──────────────┐        1        ┌─────────────────┐
│    Category    │                         │   Voucher    │◄────────────────│  OrderVoucher   │
└────────────────┘                         └──────────────┘                 │   (Order Link)  │
                                                                            └─────────────────┘
```

### Giải thích:
1. **Promotion áp dụng cho Product theo 3 cách**:
   - `ALL`: Áp dụng tất cả sản phẩm
   - `PRODUCT`: Áp dụng sản phẩm cụ thể (shoeId)
   - `CATEGORY`: Áp dụng theo danh mục (categoryId)

2. **OrderVoucher**: Là bảng trung gian liên kết **Voucher** với **Order** (chưa implement Order module)
   - Lưu lịch sử sử dụng voucher
   - Lưu số tiền được giảm thực tế

---

## 4. Khi nào hiển thị thuộc tính trong Class Diagram?

### ✅ **HIỂN THỊ** trong Class Diagram:

#### 1. **Primary Key (ID)**
- Luôn hiển thị để xác định entity
- VD: `shoeId`, `campaignId`, `voucherId`

#### 2. **Thuộc tính nghiệp vụ quan trọng (Business Logic)**
- Các field quyết định logic nghiệp vụ
- VD: `status`, `enabled`, `discountType`, `targetType`

#### 3. **Thuộc tính bắt buộc (Required/NotNull)**
- Các field không được null
- VD: `name`, `basePrice`, `startDate`, `endDate`, `code`

#### 4. **Thuộc tính có ràng buộc đặc biệt (Unique, Enum)**
- VD: `code` (unique), `type` (enum), `size` (enum)

#### 5. **Foreign Key (Quan hệ)**
- Hiển thị để thể hiện mối quan hệ
- VD: `categoryId`, `shoeId`, `campaignId`

#### 6. **Thuộc tính quan trọng cho tính toán**
- VD: `discountValue`, `maxDiscountAmount`, `basePrice`

### ❌ **KHÔNG HIỂN THỊ** trong Class Diagram:

#### 1. **Metadata/Audit Fields**
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Lý do: Không liên quan đến logic nghiệp vụ

#### 2. **Thuộc tính mô tả đơn giản**
- `description`: Chỉ là text mô tả, không ảnh hưởng logic
- **Ngoại lệ**: Nếu có validation đặc biệt thì hiển thị

#### 3. **Thuộc tính kỹ thuật (Technical Fields)**
- `version` (Optimistic locking)
- `rowVersion`, `hash`

#### 4. **Thuộc tính có thể suy ra từ thuộc tính khác**
- VD: `totalStock` (có thể tính từ sum của variants)
- VD: `status` của Campaign (tự động tính từ `enabled` + `startDate` + `endDate`)

### 🎯 **Nguyên tắc chung:**
> "Chỉ hiển thị những thuộc tính giúp hiểu rõ **cấu trúc** và **logic nghiệp vụ** của hệ thống. Bỏ qua các thuộc tính mang tính **kỹ thuật** hoặc **metadata**."

---

## 5. Database Schema Chi tiết

### 📦 Bảng 1: `category`
```sql
CREATE TABLE category (
    "categoryId" BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Index
CREATE INDEX idx_category_name ON category(name);
```

**Mô tả**: Danh mục sản phẩm (VD: SNEAKER, RUNNING, CASUAL)

---

### 📦 Bảng 2: `shoes` (Sản phẩm chính)
```sql
CREATE TABLE shoes (
    "shoeId" BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    brand VARCHAR(100),
    type VARCHAR(50) NOT NULL,            -- Enum: FOR_MALE, FOR_FEMALE, FOR_UNISEX
    "basePrice" NUMERIC(15, 2) NOT NULL,
    description TEXT,
    collection VARCHAR(255),
    status BOOLEAN DEFAULT TRUE NOT NULL, -- TRUE = Đang bán, FALSE = Ngừng bán
    "categoryId" BIGINT,                  -- FK to category (nullable)
    
    CONSTRAINT fk_shoes_category 
        FOREIGN KEY ("categoryId") 
        REFERENCES category("categoryId") 
        ON DELETE SET NULL,
    
    CONSTRAINT chk_shoes_type 
        CHECK (type IN ('FOR_MALE', 'FOR_FEMALE', 'FOR_UNISEX'))
);

-- Indexes
CREATE INDEX idx_shoes_category ON shoes("categoryId");
CREATE INDEX idx_shoes_type ON shoes(type);
CREATE INDEX idx_shoes_brand ON shoes(brand);
CREATE INDEX idx_shoes_name ON shoes(name);
CREATE INDEX idx_shoes_status ON shoes(status);
```

**Khóa ngoại**:
- `categoryId` → `category(categoryId)` 
  - ON DELETE: **SET NULL** (Xóa category không xóa sản phẩm, chỉ set NULL)

---

### 📦 Bảng 3: `shoes_image` (Hình ảnh sản phẩm)
```sql
CREATE TABLE shoes_image (
    "imageId" BIGSERIAL PRIMARY KEY,
    url VARCHAR(1000) NOT NULL,
    "isThumbnail" BOOLEAN DEFAULT FALSE,
    "shoeId" BIGINT NOT NULL,             -- FK to shoes (not null)
    
    CONSTRAINT fk_image_shoes 
        FOREIGN KEY ("shoeId") 
        REFERENCES shoes("shoeId") 
        ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_image_shoes ON shoes_image("shoeId");
CREATE INDEX idx_image_thumbnail ON shoes_image("shoeId", "isThumbnail") 
    WHERE "isThumbnail" = TRUE;
```

**Khóa ngoại**:
- `shoeId` → `shoes(shoeId)`
  - ON DELETE: **CASCADE** (Xóa sản phẩm → xóa tất cả ảnh)

---

### 📦 Bảng 4: `shoes_variant` (Biến thể màu-size)
```sql
CREATE TABLE shoes_variant (
    "variantId" BIGSERIAL PRIMARY KEY,
    size VARCHAR(50) NOT NULL,            -- Enum: SIZE_35, SIZE_36, ..., SIZE_45
    color VARCHAR(50) NOT NULL,           -- Enum: BLACK, WHITE, RED, GRAY, BROWN, PINK, BLUE, GREEN
    stock INT DEFAULT 0,                  -- Tồn kho (nullable, quản lý riêng)
    "shoeId" BIGINT NOT NULL,             -- FK to shoes (not null)
    
    CONSTRAINT fk_variant_shoes 
        FOREIGN KEY ("shoeId") 
        REFERENCES shoes("shoeId") 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_variant_size 
        CHECK (size IN ('SIZE_35', 'SIZE_36', 'SIZE_37', 'SIZE_38', 'SIZE_39', 
                        'SIZE_40', 'SIZE_41', 'SIZE_42', 'SIZE_43', 'SIZE_44', 'SIZE_45')),
    
    CONSTRAINT chk_variant_color 
        CHECK (color IN ('BLACK', 'WHITE', 'RED', 'GRAY', 'BROWN', 'PINK', 'BLUE', 'GREEN'))
);

-- Indexes
CREATE INDEX idx_variant_shoes ON shoes_variant("shoeId");
CREATE INDEX idx_variant_size_color ON shoes_variant("shoeId", size, color);
CREATE INDEX idx_variant_stock ON shoes_variant(stock) WHERE stock > 0;
```

**Khóa ngoại**:
- `shoeId` → `shoes(shoeId)`
  - ON DELETE: **CASCADE** (Xóa sản phẩm → xóa tất cả biến thể)

**Lưu ý**: `stock` nullable, được quản lý bởi module riêng (không phải Admin Product)

---

### 📦 Bảng 5: `promotioncampaign` (Chiến dịch khuyến mãi)
```sql
CREATE TABLE promotioncampaign (
    "campaignId" BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    "startDate" DATE NOT NULL,
    "endDate" DATE NOT NULL,
    "discountType" VARCHAR(50) NOT NULL,        -- Enum: PERCENTAGE, FIXED_AMOUNT
    "discountValue" NUMERIC(15, 2) NOT NULL,
    "maxDiscountAmount" NUMERIC(15, 2),
    "minOrderValue" NUMERIC(15, 2),
    status VARCHAR(50) NOT NULL,                -- Enum: DRAFT, ACTIVE, ENDED, CANCELLED
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    
    CONSTRAINT chk_campaign_discount_type 
        CHECK ("discountType" IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    
    CONSTRAINT chk_campaign_status 
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ENDED', 'CANCELLED')),
    
    CONSTRAINT chk_campaign_dates 
        CHECK ("endDate" >= "startDate")
);

-- Indexes
CREATE INDEX idx_campaign_dates ON promotioncampaign("startDate", "endDate");
CREATE INDEX idx_campaign_status ON promotioncampaign(status);
CREATE INDEX idx_campaign_enabled ON promotioncampaign(enabled);
```

**Lưu ý**: 
- `status` tự động tính toán từ `enabled`, `startDate`, `endDate`
- `enabled = FALSE` → `status = CANCELLED`

---

### 📦 Bảng 6: `promotiontarget` (Đối tượng áp dụng khuyến mãi)
```sql
CREATE TABLE promotiontarget (
    "targetId" BIGSERIAL PRIMARY KEY,
    "targetType" VARCHAR(50) NOT NULL,     -- Enum: ALL, PRODUCT, CATEGORY
    "shoeId" BIGINT,                       -- FK to shoes (nullable, chỉ dùng khi targetType = PRODUCT)
    "categoryId" BIGINT,                   -- FK to category (nullable, chỉ dùng khi targetType = CATEGORY)
    "campaignId" BIGINT NOT NULL,          -- FK to promotioncampaign (not null)
    
    CONSTRAINT fk_target_shoes 
        FOREIGN KEY ("shoeId") 
        REFERENCES shoes("shoeId") 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_target_category 
        FOREIGN KEY ("categoryId") 
        REFERENCES category("categoryId") 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_target_campaign 
        FOREIGN KEY ("campaignId") 
        REFERENCES promotioncampaign("campaignId") 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_target_type 
        CHECK ("targetType" IN ('ALL', 'PRODUCT', 'CATEGORY')),
    
    CONSTRAINT chk_target_consistency 
        CHECK (
            ("targetType" = 'ALL' AND "shoeId" IS NULL AND "categoryId" IS NULL) OR
            ("targetType" = 'PRODUCT' AND "shoeId" IS NOT NULL AND "categoryId" IS NULL) OR
            ("targetType" = 'CATEGORY' AND "shoeId" IS NULL AND "categoryId" IS NOT NULL)
        )
);

-- Indexes
CREATE INDEX idx_target_campaign ON promotiontarget("campaignId");
CREATE INDEX idx_target_shoe ON promotiontarget("shoeId");
CREATE INDEX idx_target_category ON promotiontarget("categoryId");
CREATE INDEX idx_target_type ON promotiontarget("targetType");
```

**Khóa ngoại**:
- `campaignId` → `promotioncampaign(campaignId)` - ON DELETE: **CASCADE**
- `shoeId` → `shoes(shoeId)` - ON DELETE: **CASCADE** (Optional)
- `categoryId` → `category(categoryId)` - ON DELETE: **CASCADE** (Optional)

**Logic**:
- `targetType = ALL`: Áp dụng tất cả sản phẩm
- `targetType = PRODUCT`: Áp dụng sản phẩm cụ thể (shoeId NOT NULL)
- `targetType = CATEGORY`: Áp dụng theo danh mục (categoryId NOT NULL)

---

### 📦 Bảng 7: `voucher` (Mã giảm giá)
```sql
CREATE TABLE voucher (
    "voucherId" BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255),
    description TEXT,
    "discountType" VARCHAR(50) NOT NULL,        -- Enum: PERCENTAGE, FIXED_AMOUNT
    "discountValue" NUMERIC(15, 2) NOT NULL,
    "maxDiscountValue" NUMERIC(15, 2),
    "minOrderValue" NUMERIC(15, 2),
    "startDate" DATE NOT NULL,
    "endDate" DATE NOT NULL,
    "maxRedeemPerCustomer" BIGINT,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    "campaignId" BIGINT NOT NULL,               -- FK to promotioncampaign (not null)
    
    CONSTRAINT fk_voucher_campaign 
        FOREIGN KEY ("campaignId") 
        REFERENCES promotioncampaign("campaignId") 
        ON DELETE CASCADE,
    
    CONSTRAINT chk_voucher_discount_type 
        CHECK ("discountType" IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    
    CONSTRAINT chk_voucher_dates 
        CHECK ("endDate" >= "startDate")
);

-- Indexes
CREATE UNIQUE INDEX idx_voucher_code ON voucher(code);
CREATE INDEX idx_voucher_campaign ON voucher("campaignId");
CREATE INDEX idx_voucher_dates ON voucher("startDate", "endDate");
CREATE INDEX idx_voucher_enabled ON voucher(enabled);
```

**Khóa ngoại**:
- `campaignId` → `promotioncampaign(campaignId)`
  - ON DELETE: **CASCADE** (Xóa campaign → xóa tất cả voucher)

**Lưu ý**:
- `code` phải UNIQUE
- Voucher có quy tắc giảm giá riêng (không kế thừa từ campaign)

---

### 📦 Bảng 8: `ordervoucher` (Liên kết Voucher với Order)
```sql
CREATE TABLE ordervoucher (
    "orderVoucherId" BIGSERIAL PRIMARY KEY,
    "orderId" BIGINT NOT NULL,                  -- FK to orders (chưa implement)
    "voucherId" BIGINT NOT NULL,                -- FK to voucher (not null)
    "userId" BIGINT,                            -- FK to users (optional)
    "appliedAmount" NUMERIC(15, 2) NOT NULL,    -- Số tiền được giảm thực tế
    "createdAt" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ordervoucher_voucher 
        FOREIGN KEY ("voucherId") 
        REFERENCES voucher("voucherId") 
        ON DELETE RESTRICT
    
    -- CONSTRAINT fk_ordervoucher_order (chưa implement)
    -- CONSTRAINT fk_ordervoucher_user (chưa implement)
);

-- Indexes
CREATE INDEX idx_ordervoucher_order ON ordervoucher("orderId");
CREATE INDEX idx_ordervoucher_voucher ON ordervoucher("voucherId");
CREATE INDEX idx_ordervoucher_user ON ordervoucher("userId");
```

**Khóa ngoại**:
- `voucherId` → `voucher(voucherId)`
  - ON DELETE: **RESTRICT** (Không cho xóa voucher đã được sử dụng)

**Chưa implement**: Order module, nên `orderId` và `userId` chưa có FK constraint

---

## 📊 Tổng kết Database Schema

### Quan hệ tổng quan:

```
category (1) ─────────> (*) shoes (1) ────┬───> (*) shoes_image
                                          │
                                          └───> (*) shoes_variant

promotioncampaign (1) ──┬──> (*) voucher (1) ───> (*) ordervoucher
                        │                               (Link to Order)
                        └──> (*) promotiontarget
                                    │
                                    ├──> (Optional) shoes
                                    │
                                    └──> (Optional) category
```

### Cascade Rules:
| Parent → Child | ON DELETE | Lý do |
|---|---|---|
| `shoes` → `shoes_image` | CASCADE | Xóa sản phẩm → xóa ảnh |
| `shoes` → `shoes_variant` | CASCADE | Xóa sản phẩm → xóa biến thể |
| `category` → `shoes` | SET NULL | Xóa danh mục → sản phẩm vẫn tồn tại |
| `promotioncampaign` → `voucher` | CASCADE | Xóa campaign → xóa voucher |
| `promotioncampaign` → `promotiontarget` | CASCADE | Xóa campaign → xóa target |
| `voucher` → `ordervoucher` | RESTRICT | Không xóa voucher đã dùng |
| `promotiontarget` → `shoes` | CASCADE | Xóa sản phẩm → xóa target |
| `promotiontarget` → `category` | CASCADE | Xóa danh mục → xóa target |

---

## 🎓 Kết luận

### Class Diagram tập trung vào:
1. ✅ Primary Keys
2. ✅ Business Logic Fields (status, enabled, type)
3. ✅ Required Fields (NotNull)
4. ✅ Relationships (Foreign Keys)
5. ✅ Important Calculations (price, discount)

### Database Schema chi tiết:
1. ✅ Tất cả columns
2. ✅ Constraints (CHECK, UNIQUE)
3. ✅ Foreign Keys với ON DELETE rules
4. ✅ Indexes cho performance

**Nguyên tắc**: Class Diagram cho **understanding**, Database Schema cho **implementation**.
