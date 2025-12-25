-- -- ============================================================================
-- -- WEBSHOE DATABASE SETUP - MATCH WITH JAVA ENTITY CODE
-- -- Phiên bản ngày 13/12/2025
-- -- ============================================================================

-- -- ============================================================================
-- -- DROP EXISTING TABLES (Reset database nếu cần)
-- -- ============================================================================
-- DROP TABLE IF EXISTS shoes_variant CASCADE;
-- DROP TABLE IF EXISTS shoes_image CASCADE;
-- DROP TABLE IF EXISTS shoes CASCADE;
-- DROP TABLE IF EXISTS category CASCADE;

-- -- ============================================================================
-- -- 1. CATEGORY TABLE
-- -- Match với: ecommerce.shoestore.category.Category
-- -- ============================================================================
-- CREATE TABLE category (
--     "categoryId" BIGSERIAL PRIMARY KEY,
--     name VARCHAR(255) NOT NULL UNIQUE
-- );

-- -- Index
-- CREATE INDEX idx_category_name ON category(name);

-- -- ============================================================================
-- -- 2. SHOES TABLE
-- -- Match với: ecommerce.shoestore.shoes.Shoes
-- -- ============================================================================
-- CREATE TABLE shoes (
--     "shoeId" BIGSERIAL PRIMARY KEY,
--     name VARCHAR(500) NOT NULL,
--     brand VARCHAR(100),
--     type VARCHAR(50) NOT NULL,
--     "basePrice" NUMERIC(15, 2) NOT NULL,
--     description TEXT,
--     collection VARCHAR(255),
--     "categoryId" BIGINT REFERENCES category("categoryId") ON DELETE SET NULL,
--     "createdAt" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
--     CONSTRAINT chk_shoes_type CHECK (type IN ('FOR_MALE', 'FOR_FEMALE', 'FOR_UNISEX'))
-- );

-- -- Indexes
-- CREATE INDEX idx_shoes_category ON shoes("categoryId");
-- CREATE INDEX idx_shoes_type ON shoes(type);
-- CREATE INDEX idx_shoes_brand ON shoes(brand);
-- CREATE INDEX idx_shoes_name ON shoes(name);

-- -- ============================================================================
-- -- 3. SHOES_IMAGE TABLE
-- -- Match với: ecommerce.shoestore.shoesimage.ShoesImage
-- -- ============================================================================
-- CREATE TABLE shoes_image (
--     "imageId" BIGSERIAL PRIMARY KEY,
--     url VARCHAR(1000) NOT NULL,
--     "isThumbnail" BOOLEAN DEFAULT FALSE,
--     "shoeId" BIGINT NOT NULL REFERENCES shoes("shoeId") ON DELETE CASCADE
-- );

-- -- Indexes
-- CREATE INDEX idx_image_shoes ON shoes_image("shoeId");
-- CREATE INDEX idx_image_thumbnail ON shoes_image("shoeId", "isThumbnail") WHERE "isThumbnail" = TRUE;

-- -- ============================================================================
-- -- 4. SHOES_VARIANT TABLE
-- -- Match với: ecommerce.shoestore.shoesvariant.ShoesVariant
-- -- Size/Color values match với enum trong Java
-- -- ============================================================================
-- CREATE TABLE shoes_variant (
--     "variantId" BIGSERIAL PRIMARY KEY,
--     size VARCHAR(50) NOT NULL,
--     color VARCHAR(50) NOT NULL,
--     stock INT DEFAULT 0,
--     "shoeId" BIGINT NOT NULL REFERENCES shoes("shoeId") ON DELETE CASCADE,
    
--     -- Size values match với enum Size.java: SIZE_35, SIZE_36, ..., SIZE_45
--     CONSTRAINT chk_variant_size CHECK (size IN ('SIZE_35', 'SIZE_36', 'SIZE_37', 'SIZE_38', 'SIZE_39', 'SIZE_40', 'SIZE_41', 'SIZE_42', 'SIZE_43', 'SIZE_44', 'SIZE_45')),
    
--     -- Color values match với enum Color.java: BLACK, WHITE, RED, GRAY, BROWN, PINK, BLUE, GREEN
--     CONSTRAINT chk_variant_color CHECK (color IN ('BLACK', 'WHITE', 'RED', 'GRAY', 'BROWN', 'PINK', 'BLUE', 'GREEN'))
-- );

-- -- Indexes
-- CREATE INDEX idx_variant_shoes ON shoes_variant("shoeId");
-- CREATE INDEX idx_variant_size_color ON shoes_variant("shoeId", size, color);
-- CREATE INDEX idx_variant_stock ON shoes_variant(stock) WHERE stock > 0;

-- -- ============================================================================
-- -- INSERT SAMPLE DATA
-- -- ============================================================================

-- -- ============================================================================
-- -- CATEGORIES
-- -- ============================================================================
-- INSERT INTO category (name) VALUES
-- ('SNEAKER'),
-- ('RUNNING'),
-- ('CASUAL'),
-- ('SPORTS'),
-- ('SANDAL'),
-- ('BOOTS');

-- -- ============================================================================
-- -- SHOES DATA - Thương hiệu Việt Nam và Quốc tế
-- -- ============================================================================

-- -- 1. Biti's Hunter Litedash Nam
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Litedash Nam', 'BITIS', 'FOR_MALE', 1069000, 
-- 'Giày thể thao Bitis Hunter Litedash với công nghệ LiteFoam siêu nhẹ. Thiết kế năng động, phù hợp cho chạy bộ và hoạt động hàng ngày. Đế cao su chống trượt, thoáng khí.',
-- 'Hunter 2024', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800', TRUE, 1),
-- ('https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800', FALSE, 1),
-- ('https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800', FALSE, 1);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'BLACK', 15, 1), ('SIZE_40', 'BLACK', 20, 1), ('SIZE_41', 'BLACK', 18, 1),
-- ('SIZE_42', 'BLACK', 12, 1), ('SIZE_43', 'BLACK', 8, 1),
-- ('SIZE_39', 'WHITE', 10, 1), ('SIZE_40', 'WHITE', 15, 1), ('SIZE_41', 'WHITE', 14, 1);

-- -- 2. Biti's Hunter Core Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Core Nữ', 'BITIS', 'FOR_FEMALE', 845000,
-- 'Giày thể thao nữ Bitis Hunter Core thiết kế hiện đại, đệm êm ái. Phong cách năng động, phù hợp mọi hoạt động. Chất liệu Primeknit co giãn tốt.',
-- 'Hunter Core 2024', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800', TRUE, 2),
-- ('https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800', FALSE, 2),
-- ('https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?w=800', FALSE, 2);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'PINK', 20, 2), ('SIZE_36', 'PINK', 25, 2), ('SIZE_37', 'PINK', 30, 2),
-- ('SIZE_38', 'PINK', 22, 2), ('SIZE_39', 'PINK', 15, 2),
-- ('SIZE_35', 'WHITE', 18, 2), ('SIZE_36', 'WHITE', 20, 2), ('SIZE_37', 'WHITE', 25, 2);

-- -- 3. Biti's Soulmate Nam
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Soulmate Nam', 'BITIS', 'FOR_MALE', 545000,
-- 'Giày thể thao Bitis Soulmate phong cách tối giản. Đế cao su bền bỉ, thiết kế cổ điển. Phù hợp đi học, đi làm và dạo phố.',
-- 'Soulmate 2024', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=800', TRUE, 3),
-- ('https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=800', FALSE, 3);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'BLACK', 30, 3), ('SIZE_40', 'BLACK', 35, 3), ('SIZE_41', 'BLACK', 40, 3),
-- ('SIZE_42', 'BLACK', 28, 3), ('SIZE_43', 'BLACK', 20, 3),
-- ('SIZE_39', 'WHITE', 25, 3), ('SIZE_40', 'WHITE', 30, 3);

-- -- 4. Nike Air Max 270
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Nike Air Max 270 React', 'NIKE', 'FOR_UNISEX', 3500000,
-- 'Giày thể thao cao cấp với công nghệ Air Max mang lại sự thoải mái tối đa. Thiết kế hiện đại với đệm khí lớn nhất từ trước đến nay.',
-- 'Air Max 2024', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1514989940723-e8709f5b9fc4?w=800', TRUE, 4),
-- ('https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800', FALSE, 4),
-- ('https://images.unsplash.com/photo-1491553895911-0055uj6l82c2d?w=800', FALSE, 4);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_40', 'BLACK', 10, 4), ('SIZE_41', 'BLACK', 12, 4), ('SIZE_42', 'BLACK', 15, 4),
-- ('SIZE_43', 'BLACK', 8, 4), ('SIZE_44', 'BLACK', 5, 4),
-- ('SIZE_40', 'WHITE', 8, 4), ('SIZE_41', 'WHITE', 10, 4);

-- -- 5. Adidas Ultraboost 22
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Adidas Ultraboost 22', 'ADIDAS', 'FOR_MALE', 4200000,
-- 'Giày chạy bộ với công nghệ Boost mang lại năng lượng trở lại mỗi bước chạy. Thượng vải Primeknit co giãn tốt, thoáng khí.',
-- 'Ultraboost 2024', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800', TRUE, 5),
-- ('https://images.unsplash.com/photo-1606890658317-7d14490b76fd?w=800', FALSE, 5);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_40', 'BLACK', 8, 5), ('SIZE_41', 'BLACK', 10, 5), ('SIZE_42', 'BLACK', 12, 5),
-- ('SIZE_43', 'BLACK', 7, 5), ('SIZE_44', 'BLACK', 4, 5),
-- ('SIZE_40', 'GRAY', 6, 5), ('SIZE_41', 'GRAY', 8, 5);

-- -- 6. Biti's Hunter Easy Fit Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Easy Fit Nữ', 'BITIS', 'FOR_FEMALE', 1265000,
-- 'Giày thể thao cao cấp Bitis Hunter Easy Fit với công nghệ đệm tiên tiến. Thiết kế thời trang, êm ái suốt ngày dài.',
-- 'Hunter Premium 2024', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1539185441755-769473a23570?w=800', TRUE, 6),
-- ('https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800', FALSE, 6);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'PINK', 15, 6), ('SIZE_36', 'PINK', 18, 6), ('SIZE_37', 'PINK', 20, 6),
-- ('SIZE_38', 'PINK', 12, 6), ('SIZE_39', 'PINK', 8, 6);

-- -- 7. Nike Air Force 1
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Nike Air Force 1 Low', 'NIKE', 'FOR_UNISEX', 2800000,
-- 'Biểu tượng của Nike với thiết kế cổ điển từ năm 1982. Đế cao su bền bỉ, phong cách đường phố.',
-- 'Air Force Classics', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800', TRUE, 7),
-- ('https://images.unsplash.com/photo-1595341888016-a392ef81b7de?w=800', FALSE, 7);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'WHITE', 20, 7), ('SIZE_40', 'WHITE', 25, 7), ('SIZE_41', 'WHITE', 30, 7),
-- ('SIZE_42', 'WHITE', 22, 7), ('SIZE_43', 'WHITE', 15, 7), ('SIZE_44', 'WHITE', 10, 7);

-- -- 8. Biti's Embrace Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Embrace Nữ', 'BITIS', 'FOR_FEMALE', 575000,
-- 'Giày thể thao nữ Bitis Embrace nhẹ nhàng, thoải mái. Thiết kế trẻ trung, phù hợp mọi hoạt động.',
-- 'Embrace 2024', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800', TRUE, 8),
-- ('https://images.unsplash.com/photo-1465877783223-4eba513e27c6?w=800', FALSE, 8);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'WHITE', 25, 8), ('SIZE_36', 'WHITE', 30, 8), ('SIZE_37', 'WHITE', 35, 8),
-- ('SIZE_38', 'WHITE', 28, 8), ('SIZE_39', 'WHITE', 20, 8),
-- ('SIZE_35', 'GRAY', 15, 8), ('SIZE_36', 'GRAY', 18, 8);

-- -- 9. Adidas Stan Smith
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Adidas Stan Smith Classic', 'ADIDAS', 'FOR_UNISEX', 2500000,
-- 'Giày thể thao cổ điển với thiết kế tối giản. Chất liệu da cao cấp, phong cách vượt thời gian.',
-- 'Stan Smith Originals', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1603808033192-082d6919d3e1?w=800', TRUE, 9),
-- ('https://images.unsplash.com/photo-1584735175315-9d5df23860e6?w=800', FALSE, 9);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_38', 'WHITE', 12, 9), ('SIZE_39', 'WHITE', 15, 9), ('SIZE_40', 'WHITE', 18, 9),
-- ('SIZE_41', 'WHITE', 20, 9), ('SIZE_42', 'WHITE', 16, 9), ('SIZE_43', 'WHITE', 10, 9);

-- -- 10. Biti's Hunter Street Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Street Nữ', 'BITIS', 'FOR_FEMALE', 685000,
-- 'Giày thể thao đường phố Bitis Hunter Street. Phong cách trendy, đế chunky cá tính.',
-- 'Hunter Street 2024', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1552346154-21d32810aba3?w=800', TRUE, 10),
-- ('https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800', FALSE, 10);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'GRAY', 18, 10), ('SIZE_36', 'GRAY', 22, 10), ('SIZE_37', 'GRAY', 25, 10),
-- ('SIZE_38', 'GRAY', 20, 10), ('SIZE_39', 'GRAY', 15, 10),
-- ('SIZE_35', 'BROWN', 12, 10), ('SIZE_36', 'BROWN', 15, 10);

-- -- 11. Nike ZoomX Vaporfly
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Nike ZoomX Vaporfly NEXT%', 'NIKE', 'FOR_MALE', 6500000,
-- 'Giày chạy bộ chuyên nghiệp với công nghệ ZoomX Foam. Được các VĐV Marathon ưa chuộng. Siêu nhẹ, siêu nhanh.',
-- 'Racing Elite 2024', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1606890658317-7d14490b76fd?w=800', TRUE, 11),
-- ('https://images.unsplash.com/photo-1597045566677-8cf032ed6634?w=800', FALSE, 11);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_40', 'GREEN', 5, 11), ('SIZE_41', 'GREEN', 7, 11), ('SIZE_42', 'GREEN', 8, 11),
-- ('SIZE_43', 'GREEN', 4, 11), ('SIZE_44', 'GREEN', 3, 11);

-- -- 12. Biti's Basic Nam
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Basic Nam BSM000600', 'BITIS', 'FOR_MALE', 405000,
-- 'Giày thể thao Bitis Basic giá tốt, chất lượng đảm bảo. Phù hợp đi học, đi làm. Đã bán hơn 29.000 đôi.',
-- 'Basic Collection', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1491553895911-0055uj6l82c2d?w=800', TRUE, 12),
-- ('https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=800', FALSE, 12);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'BLACK', 50, 12), ('SIZE_40', 'BLACK', 60, 12), ('SIZE_41', 'BLACK', 55, 12),
-- ('SIZE_42', 'BLACK', 45, 12), ('SIZE_43', 'BLACK', 35, 12),
-- ('SIZE_39', 'WHITE', 40, 12), ('SIZE_40', 'WHITE', 50, 12);

-- -- 13. Puma RS-X
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Puma RS-X Reinvention', 'PUMA', 'FOR_UNISEX', 2900000,
-- 'Giày thể thao phong cách retro-futuristic. Đế Running System mang lại sự thoải mái. Thiết kế chunky độc đáo.',
-- 'RS Collection 2024', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800', TRUE, 13),
-- ('https://images.unsplash.com/photo-1605348532760-6753d2c43329?w=800', FALSE, 13);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'WHITE', 10, 13), ('SIZE_40', 'WHITE', 12, 13), ('SIZE_41', 'WHITE', 15, 13),
-- ('SIZE_42', 'WHITE', 11, 13), ('SIZE_43', 'WHITE', 8, 13),
-- ('SIZE_40', 'BLUE', 6, 13), ('SIZE_41', 'BLUE', 8, 13);

-- -- 14. Biti's Hunter Litebound Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Litebound Nữ', 'BITIS', 'FOR_FEMALE', 1175000,
-- 'Giày thể thao cao cấp Bitis Hunter Litebound. Công nghệ LiteFoam siêu nhẹ, đệm êm ái. Thiết kế thời trang.',
-- 'Hunter Premium 2024', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?w=800', TRUE, 14),
-- ('https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800', FALSE, 14);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'WHITE', 12, 14), ('SIZE_36', 'WHITE', 15, 14), ('SIZE_37', 'WHITE', 18, 14),
-- ('SIZE_38', 'WHITE', 14, 14), ('SIZE_39', 'WHITE', 10, 14);

-- -- 15. New Balance 574
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('New Balance 574 Classic', 'NEW BALANCE', 'FOR_UNISEX', 2350000,
-- 'Biểu tượng của New Balance từ những năm 80. Thiết kế cổ điển, đệm ENCAP bền bỉ.',
-- '574 Heritage', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1539185441755-769473a23570?w=800', TRUE, 15),
-- ('https://images.unsplash.com/photo-1584735175315-9d5df23860e6?w=800', FALSE, 15);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'GRAY', 15, 15), ('SIZE_40', 'GRAY', 18, 15), ('SIZE_41', 'GRAY', 20, 15),
-- ('SIZE_42', 'GRAY', 16, 15), ('SIZE_43', 'GRAY', 12, 15);

-- -- 16. Biti's Eva E-Flow Nam (Giá rẻ - bán chạy)
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Eva E-Flow Nam', 'BITIS', 'FOR_MALE', 240000,
-- 'Giày thể thao Eva Bitis E-Flow siêu nhẹ, giá tốt. Chất liệu EVA một khối, thoáng khí. Đã bán hơn 2.300 đôi.',
-- 'E-Flow Budget', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800', TRUE, 16),
-- ('https://images.unsplash.com/photo-1595341888016-a392ef81b7de?w=800', FALSE, 16);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'BLACK', 80, 16), ('SIZE_40', 'BLACK', 100, 16), ('SIZE_41', 'BLACK', 90, 16),
-- ('SIZE_42', 'BLACK', 70, 16), ('SIZE_43', 'BLACK', 50, 16),
-- ('SIZE_39', 'BLUE', 40, 16), ('SIZE_40', 'BLUE', 50, 16);

-- -- 17. Converse Chuck Taylor (Out of Stock - để test)
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Converse Chuck Taylor All Star', 'CONVERSE', 'FOR_UNISEX', 1500000,
-- 'Giày thể thao huyền thoại Chuck Taylor. Thiết kế canvas cổ điển, đế cao su vulcanized.',
-- 'Chuck Taylor Originals', 3);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1463100099107-aa0980c362e6?w=800', TRUE, 17);

-- -- Không có variant với stock > 0 => Out of Stock
-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_39', 'BLACK', 0, 17), ('SIZE_40', 'BLACK', 0, 17), ('SIZE_41', 'BLACK', 0, 17);

-- -- 18. Vans Old Skool
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Vans Old Skool Classic', 'VANS', 'FOR_UNISEX', 1800000,
-- 'Giày trượt ván huyền thoại với sọc Jazz Stripe đặc trưng. Đế waffle grip tốt, phong cách đường phố.',
-- 'Old Skool Classics', 1);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800', TRUE, 18),
-- ('https://images.unsplash.com/photo-1465877783223-4eba513e27c6?w=800', FALSE, 18);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_38', 'BLACK', 20, 18), ('SIZE_39', 'BLACK', 25, 18), ('SIZE_40', 'BLACK', 30, 18),
-- ('SIZE_41', 'BLACK', 28, 18), ('SIZE_42', 'BLACK', 22, 18), ('SIZE_43', 'BLACK', 15, 18);

-- -- 19. Biti's Hunter Lunar Dash Nữ
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Bitis Hunter Lunar Dash Nữ', 'BITIS', 'FOR_FEMALE', 965000,
-- 'Giày thể thao Bitis Hunter Lunar Dash thiết kế độc đáo. Công nghệ đệm mới, phong cách cá tính.',
-- 'Hunter Lunar 2024', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800', TRUE, 19),
-- ('https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800', FALSE, 19);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_35', 'RED', 10, 19), ('SIZE_36', 'RED', 12, 19), ('SIZE_37', 'RED', 15, 19),
-- ('SIZE_38', 'RED', 11, 19), ('SIZE_39', 'RED', 8, 19);

-- -- 20. Asics Gel-Kayano
-- INSERT INTO shoes (name, brand, type, "basePrice", description, collection, "categoryId") VALUES
-- ('Asics Gel-Kayano 30', 'ASICS', 'FOR_MALE', 4500000,
-- 'Giày chạy bộ chuyên nghiệp với công nghệ GEL siêu êm. Hỗ trợ ổn định cho runner pronation.',
-- 'Performance Running', 2);

-- INSERT INTO shoes_image (url, "isThumbnail", "shoeId") VALUES
-- ('https://images.unsplash.com/photo-1606890658317-7d14490b76fd?w=800', TRUE, 20),
-- ('https://images.unsplash.com/photo-1597045566677-8cf032ed6634?w=800', FALSE, 20);

-- INSERT INTO shoes_variant (size, color, stock, "shoeId") VALUES
-- ('SIZE_40', 'BLUE', 6, 20), ('SIZE_41', 'BLUE', 8, 20), ('SIZE_42', 'BLUE', 10, 20),
-- ('SIZE_43', 'BLUE', 7, 20), ('SIZE_44', 'BLUE', 4, 20);

-- -- ============================================================================
-- -- VERIFICATION QUERIES
-- -- ============================================================================
-- -- Kiểm tra số lượng dữ liệu đã insert
-- -- SELECT 'Categories' as table_name, COUNT(*) as count FROM category
-- -- UNION ALL
-- -- SELECT 'Shoes', COUNT(*) FROM shoes
-- -- UNION ALL  
-- -- SELECT 'Images', COUNT(*) FROM shoes_image
-- -- UNION ALL
-- -- SELECT 'Variants', COUNT(*) FROM shoes_variant;

-- -- Test query giống như ShoesRepository.findAll()
-- -- SELECT DISTINCT s."shoeId", s.name, s.brand, s."basePrice", c.name as category
-- -- FROM shoes s
-- -- LEFT JOIN category c ON s."categoryId" = c."categoryId"
-- -- LEFT JOIN shoes_image si ON s."shoeId" = si."shoeId"
-- -- ORDER BY s."shoeId"
-- -- LIMIT 12;

-- -- ============================================================================
-- -- END OF SCRIPT
-- -- ============================================================================
