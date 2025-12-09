-- ============================================================================
-- SUPABASE SQL SCRIPT - SHOE STORE E-COMMERCE DATABASE
-- ============================================================================
-- Professional SQL script for a complete shoe store database
-- Ready to import into Supabase PostgreSQL
-- ============================================================================

-- ============================================================================
-- 1. CREATE CATEGORY TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_name ON category(name);

-- ============================================================================
-- 2. CREATE SHOES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS shoes (
    id BIGSERIAL PRIMARY KEY,
    shoes_id BIGINT,
    name VARCHAR(500) NOT NULL,
    brand VARCHAR(100),
    type VARCHAR(20) NOT NULL CHECK (type IN ('FOR_FEMALE', 'FOR_MALE', 'FOR_UNISEX')),
    base_price NUMERIC(15, 2) NOT NULL,
    description TEXT,
    collection VARCHAR(255),
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shoes_name ON shoes(name);
CREATE INDEX idx_shoes_brand ON shoes(brand);
CREATE INDEX idx_shoes_category ON shoes(category_id);
CREATE INDEX idx_shoes_type ON shoes(type);
CREATE INDEX idx_shoes_collection ON shoes(collection);

-- ============================================================================
-- 3. CREATE SHOES_VARIANT TABLE (Size, Color, Stock)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shoes_variant (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT,
    size VARCHAR(10) NOT NULL CHECK (size IN ('35', '36', '37', '38', '39', '40', '41', '42', '43', '44', '45')),
    color VARCHAR(20) NOT NULL CHECK (color IN ('BLACK', 'WHITE', 'RED', 'GRAY', 'BROWN', 'PINK', 'BLUE', 'GREEN')),
    stock INTEGER DEFAULT 0,
    shoes_id BIGINT NOT NULL REFERENCES shoes(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shoes_variant_shoes ON shoes_variant(shoes_id);
CREATE INDEX idx_shoes_variant_size_color ON shoes_variant(shoes_id, size, color);
CREATE INDEX idx_shoes_variant_stock ON shoes_variant(stock) WHERE stock > 0;

-- ============================================================================
-- 4. CREATE SHOES_IMAGE TABLE (Multiple Images per Product)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shoes_image (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT,
    url VARCHAR(1000) NOT NULL,
    is_thumbnail BOOLEAN DEFAULT FALSE,
    shoes_id BIGINT NOT NULL REFERENCES shoes(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shoes_image_shoes ON shoes_image(shoes_id);
CREATE INDEX idx_shoes_image_thumbnail ON shoes_image(shoes_id, is_thumbnail) WHERE is_thumbnail = TRUE;
CREATE INDEX idx_shoes_image_order ON shoes_image(shoes_id, display_order);

-- ============================================================================
-- 5. INSERT CATEGORIES
-- ============================================================================
INSERT INTO category (name, display_name) VALUES
('RUNNING', 'Giày Chạy'),
('CASUAL', 'Giày Casual'),
('FORMAL', 'Giày Lịch Sự'),
('SPORTS', 'Giày Thể Thao'),
('SANDAL', 'Dép - Xăng Đan'),
('BOOTS', 'Giày Boots');

-- ============================================================================
-- 6. INSERT SHOES - PRODUCT 1: NIKE AIR MAX 90
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    1,
    'Nike Air Max 90 Premium Leather',
    'Nike',
    'FOR_UNISEX',
    2799000.00,
    'Giày Nike Air Max 90 với thiết kế clas­sic, có đệm khí Max Air được cải tiến giúp giảm chấn tốt. Sử dụng chất liệu da cao cấp, bền bỉ và có tính thẩm mỹ cao. Phù hợp cho cả nam và nữ, thích hợp cho các hoạt động hàng ngày và giải trí. Rãnh đế giày nổi bật, cung cấp độ bám tốt và độ bền lâu dài.',
    'Summer 2024',
    2
);

-- Product 1 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(101, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', TRUE, 1, 0),
(102, 'https://images.unsplash.com/photo-1518062414763-12485bfc2604?w=800&q=80', FALSE, 1, 1),
(103, 'https://images.unsplash.com/photo-1556110639-b6a29e2dcb75?w=800&q=80', FALSE, 1, 2),
(104, 'https://images.unsplash.com/photo-1597045866556-38937dc47693?w=800&q=80', FALSE, 1, 3),
(105, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 1, 4);

-- Product 1 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(1001, '35', 'BLACK', 15, 1),
(1002, '35', 'WHITE', 8, 1),
(1003, '36', 'BLACK', 20, 1),
(1004, '36', 'WHITE', 12, 1),
(1005, '36', 'RED', 5, 1),
(1006, '37', 'BLACK', 18, 1),
(1007, '37', 'WHITE', 10, 1),
(1008, '38', 'BLACK', 25, 1),
(1009, '38', 'BLUE', 7, 1),
(1010, '39', 'BLACK', 22, 1),
(1011, '40', 'WHITE', 14, 1),
(1012, '41', 'BLACK', 11, 1),
(1013, '42', 'WHITE', 9, 1);

-- ============================================================================
-- 7. INSERT SHOES - PRODUCT 2: ADIDAS ULTRA BOOST
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    2,
    'Adidas Ultraboost 22 Performance Running',
    'Adidas',
    'FOR_MALE',
    3299000.00,
    'Giày chạy Adidas Ultraboost 22 với công nghệ Boost tân tiến, cung cấp độ nảy và sự hỗ trợ tuyệt vời. Thiết kế aerodynamic với lớp đệm Boost thế hệ mới, giúp cải thiện hiệu suất chạy. Thích hợp cho cả chạy bộ hàng ngày lẫn các cuộc thi chính thức. Chất liệu Primeknit mềm mại, thoáng khí, đảm bảo sự thoải mái suốt ngày dài.',
    'Performance 2024',
    1
);

-- Product 2 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(201, 'https://images.unsplash.com/photo-1570158268183-d296b2892a5a?w=800&q=80', TRUE, 2, 0),
(202, 'https://images.unsplash.com/photo-1463622881407-45ad4e21c86d?w=800&q=80', FALSE, 2, 1),
(203, 'https://images.unsplash.com/photo-1514989940723-e8709f5b9fc4?w=800&q=80', FALSE, 2, 2),
(204, 'https://images.unsplash.com/photo-1511885642898-4c92249e20b6?w=800&q=80', FALSE, 2, 3),
(205, 'https://images.unsplash.com/photo-1525966222134-fceba80c7d00?w=800&q=80', FALSE, 2, 4),
(206, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', FALSE, 2, 5);

-- Product 2 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(2001, '37', 'BLACK', 16, 2),
(2002, '37', 'BLUE', 9, 2),
(2003, '38', 'BLACK', 21, 2),
(2004, '38', 'BLUE', 13, 2),
(2005, '39', 'BLACK', 19, 2),
(2006, '39', 'WHITE', 6, 2),
(2007, '40', 'BLACK', 26, 2),
(2008, '40', 'BLUE', 8, 2),
(2009, '41', 'BLACK', 23, 2),
(2010, '42', 'WHITE', 15, 2),
(2011, '43', 'BLACK', 12, 2),
(2012, '44', 'BLUE', 10, 2);

-- ============================================================================
-- 8. INSERT SHOES - PRODUCT 3: CONVERSE ALL STAR
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    3,
    'Converse Chuck Taylor All Star Classic',
    'Converse',
    'FOR_UNISEX',
    1499000.00,
    'Giày Converse All Star - biểu tượng thời trang vượt thời gian. Thiết kế clas­sic với vải canvas bền bỉ, đế cao su bao quanh, cung cấp sự bảo vệ tối ưu. Nhẹ, thoáng khí, dễ kết hợp với nhiều trang phục. Phù hợp cho mọi lứa tuổi, là lựa chọn hoàn hảo cho phong cách casual hàng ngày.',
    'Timeless Classic',
    2
);

-- Product 3 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(301, 'https://images.unsplash.com/photo-1479588519022-1565021ffc85?w=800&q=80', TRUE, 3, 0),
(302, 'https://images.unsplash.com/photo-1506629082632-11fbb75434c7?w=800&q=80', FALSE, 3, 1),
(303, 'https://images.unsplash.com/photo-1449505278894-fbdc4f385df1?w=800&q=80', FALSE, 3, 2),
(304, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 3, 3),
(305, 'https://images.unsplash.com/photo-1552668473-56cd6810dc0d?w=800&q=80', FALSE, 3, 4);

-- Product 3 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(3001, '35', 'BLACK', 30, 3),
(3002, '35', 'WHITE', 25, 3),
(3003, '36', 'BLACK', 35, 3),
(3004, '36', 'WHITE', 28, 3),
(3005, '36', 'RED', 12, 3),
(3006, '37', 'BLACK', 32, 3),
(3007, '37', 'WHITE', 20, 3),
(3008, '38', 'BLACK', 40, 3),
(3009, '38', 'BLUE', 15, 3),
(3010, '39', 'BLACK', 38, 3),
(3011, '40', 'WHITE', 22, 3),
(3012, '41', 'BLACK', 18, 3),
(3013, '42', 'WHITE', 16, 3),
(3014, '43', 'BLACK', 14, 3);

-- ============================================================================
-- 9. INSERT SHOES - PRODUCT 4: PUMA RS-X BOLD
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    4,
    'Puma RS-X Bold Bold Colors',
    'Puma',
    'FOR_FEMALE',
    2199000.00,
    'Giày Puma RS-X Bold với thiết kế đảo ngược, mang lại phong cách mới lạ và độc đáo. Công nghệ lót giày RS-Cushion cải tiến cung cấp sự thoải mái tối ưu. Chất liệu hỗn hợp vải và da, bền bỉ nhưng vẫn nhẹ và thoáng khí. Màu sắc táo bạo, phù hợp cho các bạn nữ yêu thích phong cách năng động.',
    'Bold Edition 2024',
    2
);

-- Product 4 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(401, 'https://images.unsplash.com/photo-1514495173541-23ea0fefb677?w=800&q=80', TRUE, 4, 0),
(402, 'https://images.unsplash.com/photo-1525966222134-fceba80c7d00?w=800&q=80', FALSE, 4, 1),
(403, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', FALSE, 4, 2),
(404, 'https://images.unsplash.com/photo-1597045866556-38937dc47693?w=800&q=80', FALSE, 4, 3),
(405, 'https://images.unsplash.com/photo-1552667466-07d71e725c34?w=800&q=80', FALSE, 4, 4);

-- Product 4 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(4001, '35', 'PINK', 12, 4),
(4002, '35', 'WHITE', 7, 4),
(4003, '36', 'PINK', 18, 4),
(4004, '36', 'BLACK', 10, 4),
(4005, '37', 'PINK', 16, 4),
(4006, '37', 'WHITE', 8, 4),
(4007, '38', 'PINK', 22, 4),
(4008, '38', 'BLUE', 6, 4),
(4009, '39', 'PINK', 19, 4),
(4010, '40', 'WHITE', 13, 4);

-- ============================================================================
-- 10. INSERT SHOES - PRODUCT 5: NEW BALANCE 990V6
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    5,
    'New Balance 990v6 Made in USA',
    'New Balance',
    'FOR_MALE',
    4299000.00,
    'Giày New Balance 990v6 được sản xuất tại Mỹ, đại diện cho sự hoàn hảo trong thiết kế giày chạy. Công nghệ ABZORB cung cấp độ giảm chấn tuyệt vời, đáp ứng các tiêu chuẩn cao nhất của người chạy. Chất liệu da thật cao cấp, lót giày suede êm ái. Mô hình kinh điển được trang bị công nghệ hiện đại, phù hợp cho những người yêu thích phong cách retro hiệu suất cao.',
    'Premium US-Made',
    1
);

-- Product 5 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(501, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', TRUE, 5, 0),
(502, 'https://images.unsplash.com/photo-1556110639-b6a29e2dcb75?w=800&q=80', FALSE, 5, 1),
(503, 'https://images.unsplash.com/photo-1518062414763-12485bfc2604?w=800&q=80', FALSE, 5, 2),
(504, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 5, 3),
(505, 'https://images.unsplash.com/photo-1597045866556-38937dc47693?w=800&q=80', FALSE, 5, 4),
(506, 'https://images.unsplash.com/photo-1514989940723-e8709f5b9fc4?w=800&q=80', FALSE, 5, 5);

-- Product 5 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(5001, '39', 'BLACK', 11, 5),
(5002, '39', 'GRAY', 6, 5),
(5003, '40', 'BLACK', 17, 5),
(5004, '40', 'GRAY', 9, 5),
(5005, '41', 'BLACK', 14, 5),
(5006, '41', 'WHITE', 5, 5),
(5007, '42', 'BLACK', 20, 5),
(5008, '42', 'GRAY', 7, 5),
(5009, '43', 'BLACK', 16, 5),
(5010, '44', 'WHITE', 10, 5),
(5011, '45', 'BLACK', 8, 5);

-- ============================================================================
-- 11. INSERT SHOES - PRODUCT 6: VANS OLD SKOOL
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    6,
    'Vans Old Skool Pro Skateboard',
    'Vans',
    'FOR_UNISEX',
    1799000.00,
    'Giày Vans Old Skool Pro được thiết kế đặc biệt cho các vận động viên trượt ván. Công nghệ Duracap thành giày cung cấp độ bền tuyệt vời trong khu vực mục tiêu bị mài mòn. Đế Vulcanized mang lại độ bám tối ưu và cảm giác tinh tế. Vải canvas chất lượng cao kết hợp với da suede, tạo nên một chiếc giày vừa bền vừa thời trang.',
    'Pro Skate',
    2
);

-- Product 6 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(601, 'https://images.unsplash.com/photo-1503531453141-6552aef4a14f?w=800&q=80', TRUE, 6, 0),
(602, 'https://images.unsplash.com/photo-1552668473-56cd6810dc0d?w=800&q=80', FALSE, 6, 1),
(603, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', FALSE, 6, 2),
(604, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 6, 3),
(605, 'https://images.unsplash.com/photo-1497622096368-ed5a328f95d6?w=800&q=80', FALSE, 6, 4);

-- Product 6 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(6001, '35', 'BLACK', 20, 6),
(6002, '35', 'WHITE', 15, 6),
(6003, '36', 'BLACK', 25, 6),
(6004, '36', 'WHITE', 18, 6),
(6005, '37', 'BLACK', 22, 6),
(6006, '37', 'WHITE', 12, 6),
(6007, '38', 'BLACK', 30, 6),
(6008, '38', 'RED', 8, 6),
(6009, '39', 'BLACK', 28, 6),
(6010, '40', 'WHITE', 16, 6),
(6011, '41', 'BLACK', 14, 6),
(6012, '42', 'WHITE', 11, 6),
(6013, '43', 'BLACK', 9, 6);

-- ============================================================================
-- 12. INSERT SHOES - PRODUCT 7: ASICS GEL-KAYANO
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    7,
    'Asics Gel-Kayano 30 Running Cushioned',
    'Asics',
    'FOR_FEMALE',
    2899000.00,
    'Giày Asics Gel-Kayano 30 là dòng giày chạy hỗ trợ ổn định tốt nhất của Asics. Công nghệ Gel ở gót chân và giữa bàn chân cung cấp độ giảm chấn tuyệt vời, bảo vệ khớp khi chạy. Lớp chống pronation FlyteFoam Propel+ nhẹ hơn nhưng cung cấp độ bền tốt hơn. Phù hợp cho những người chạy bộ nữ tìm kiếm sự cân bằng giữa hiệu suất và sự thoải mái.',
    'Performance Women',
    1
);

-- Product 7 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(701, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', TRUE, 7, 0),
(702, 'https://images.unsplash.com/photo-1525966222134-fceba80c7d00?w=800&q=80', FALSE, 7, 1),
(703, 'https://images.unsplash.com/photo-1518062414763-12485bfc2604?w=800&q=80', FALSE, 7, 2),
(704, 'https://images.unsplash.com/photo-1463622881407-45ad4e21c86d?w=800&q=80', FALSE, 7, 3),
(705, 'https://images.unsplash.com/photo-1514495173541-23ea0fefb677?w=800&q=80', FALSE, 7, 4);

-- Product 7 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(7001, '35', 'BLUE', 10, 7),
(7002, '35', 'WHITE', 6, 7),
(7003, '36', 'BLUE', 16, 7),
(7004, '36', 'PINK', 9, 7),
(7005, '37', 'BLUE', 14, 7),
(7006, '37', 'WHITE', 7, 7),
(7007, '38', 'BLUE', 20, 7),
(7008, '38', 'PINK', 5, 7),
(7009, '39', 'BLUE', 17, 7),
(7010, '40', 'WHITE', 11, 7);

-- ============================================================================
-- 13. INSERT SHOES - PRODUCT 8: DR MARTENS 1461
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    8,
    'Dr Martens 1461 Classic Boot',
    'Dr. Martens',
    'FOR_UNISEX',
    3699000.00,
    'Giày Dr Martens 1461 - biểu tượng của phong cách punk và alternative từ những năm 1960. Được chế tạo từ da bò nguyên chất, có độ bền vô cùng cao. Đế giày Air Cushioned độc quyền cung cấp sự thoải mái tuyệt vời. Dù ban đầu có phần cứng nhưng sau khi đã nên familiar, đây sẽ là đôi giày thoải mái nhất. Phù hợp cho phong cách casual, smart-casual, thậm chí formal khi phối hợp phù hợp.',
    'Classic Heritage',
    3
);

-- Product 8 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(801, 'https://images.unsplash.com/photo-1508915535892-48ba36fdc026?w=800&q=80', TRUE, 8, 0),
(802, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80', FALSE, 8, 1),
(803, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 8, 2),
(804, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', FALSE, 8, 3),
(805, 'https://images.unsplash.com/photo-1556110639-b6a29e2dcb75?w=800&q=80', FALSE, 8, 4);

-- Product 8 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(8001, '35', 'BLACK', 8, 8),
(8002, '36', 'BLACK', 12, 8),
(8003, '37', 'BLACK', 10, 8),
(8004, '38', 'BLACK', 15, 8),
(8005, '39', 'BLACK', 13, 8),
(8006, '40', 'BLACK', 16, 8),
(8007, '41', 'BLACK', 11, 8),
(8008, '42', 'BLACK', 9, 8),
(8009, '43', 'BLACK', 7, 8),
(8010, '44', 'BLACK', 6, 8),
(8011, '45', 'BLACK', 5, 8);

-- ============================================================================
-- 14. INSERT SHOES - PRODUCT 9: SALOMON SPEEDCROSS
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    9,
    'Salomon Speedcross 6 Trail Running',
    'Salomon',
    'FOR_MALE',
    2499000.00,
    'Giày Salomon Speedcross 6 được thiết kế cho những người yêu thích chạy bộ trên đường núi và địa hình khó khăn. Công nghệ Quicklace giúp bạn dễ dàng tăng/giảm độ siết giày. Đế Contagrip® mang lại độ bám tuyệt vời trên các bề mặt ướt và khô. Khung chân hợp lý cung cấp sự ổn định tuyệt vời. Rất nhẹ, thoáng khí, phù hợp cho các chuyến phiêu lưu ngoài trời.',
    'Trail Performance',
    4
);

-- Product 9 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(901, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', TRUE, 9, 0),
(902, 'https://images.unsplash.com/photo-1463622881407-45ad4e21c86d?w=800&q=80', FALSE, 9, 1),
(903, 'https://images.unsplash.com/photo-1514989940723-e8709f5b9fc4?w=800&q=80', FALSE, 9, 2),
(904, 'https://images.unsplash.com/photo-1570158268183-d296b2892a5a?w=800&q=80', FALSE, 9, 3),
(905, 'https://images.unsplash.com/photo-1511885642898-4c92249e20b6?w=800&q=80', FALSE, 9, 4);

-- Product 9 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(9001, '39', 'BLACK', 13, 9),
(9002, '39', 'BLUE', 7, 9),
(9003, '40', 'BLACK', 19, 9),
(9004, '40', 'BLUE', 11, 9),
(9005, '41', 'BLACK', 16, 9),
(9006, '41', 'GRAY', 5, 9),
(9007, '42', 'BLACK', 23, 9),
(9008, '42', 'BLUE', 8, 9),
(9009, '43', 'BLACK', 20, 9),
(9010, '44', 'GRAY', 12, 9),
(9011, '45', 'BLACK', 9, 9);

-- ============================================================================
-- 15. INSERT SHOES - PRODUCT 10: TIMBERLAND 6-INCH BOOT
-- ============================================================================
INSERT INTO shoes (shoes_id, name, brand, type, base_price, description, collection, category_id) VALUES
(
    10,
    'Timberland 6-Inch Premium Boot Waterproof',
    'Timberland',
    'FOR_MALE',
    3999000.00,
    'Giày Timberland 6-inch Premium Boot - biểu tượng của công nhân và những người yêu thích phong cách outdoor. Được chế tạo từ da chất lượng cao kết hợp công nghệ chống thấm nước, giúp chân luôn khô ráo. Đế Vibram® cung cấp độ bám xuất sắc trên các bề mặt khác nhau. Bên trong được lót bằng vải thần tỳ, giúp giữ ấm vào mùa đông. Phù hợp cho công việc nặng, hoạt động ngoài trời, hoặc phong cách casual/smart-casual.',
    'Classic Workwear',
    6
);

-- Product 10 Images
INSERT INTO shoes_image (image_id, url, is_thumbnail, shoes_id, display_order) VALUES
(1001, 'https://images.unsplash.com/photo-1559056199-641a0ac8b8d5?w=800&q=80', TRUE, 10, 0),
(1002, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80', FALSE, 10, 1),
(1003, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', FALSE, 10, 2),
(1004, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', FALSE, 10, 3),
(1005, 'https://images.unsplash.com/photo-1556110639-b6a29e2dcb75?w=800&q=80', FALSE, 10, 4);

-- Product 10 Variants
INSERT INTO shoes_variant (variant_id, size, color, stock, shoes_id) VALUES
(10001, '39', 'BROWN', 9, 10),
(10002, '40', 'BROWN', 14, 10),
(10003, '41', 'BROWN', 11, 10),
(10004, '42', 'BROWN', 18, 10),
(10005, '43', 'BROWN', 15, 10),
(10006, '44', 'BROWN', 12, 10),
(10007, '45', 'BROWN', 8, 10);

-- ============================================================================
-- 16. VERIFY DATA - SELECT QUERIES
-- ============================================================================

-- View all categories
-- SELECT * FROM category ORDER BY id;

-- View all products with their categories
-- SELECT s.id, s.name, s.brand, c.display_name as category, s.base_price 
-- FROM shoes s 
-- LEFT JOIN category c ON s.category_id = c.id 
-- ORDER BY s.id;

-- View detailed product with all images
-- SELECT 
--     s.id, s.name, s.brand, s.type, s.base_price, s.description,
--     COUNT(DISTINCT si.id) as total_images,
--     COUNT(DISTINCT sv.id) as total_variants
-- FROM shoes s
-- LEFT JOIN shoes_image si ON s.id = si.shoes_id
-- LEFT JOIN shoes_variant sv ON s.id = sv.shoes_id
-- GROUP BY s.id, s.name, s.brand, s.type, s.base_price, s.description
-- ORDER BY s.id;

-- View product with all images and variants in detail
-- SELECT 
--     s.id as product_id,
--     s.name,
--     s.brand,
--     s.base_price,
--     si.url as image_url,
--     si.is_thumbnail,
--     sv.size,
--     sv.color,
--     sv.stock
-- FROM shoes s
-- LEFT JOIN shoes_image si ON s.id = si.shoes_id
-- LEFT JOIN shoes_variant sv ON s.id = sv.shoes_id
-- ORDER BY s.id, si.display_order, sv.id;

-- Count products by category
-- SELECT c.display_name, COUNT(s.id) as product_count
-- FROM category c
-- LEFT JOIN shoes s ON c.id = s.category_id
-- GROUP BY c.id, c.display_name
-- ORDER BY product_count DESC;

-- Check stock availability
-- SELECT 
--     s.name,
--     sv.size,
--     sv.color,
--     sv.stock
-- FROM shoes_variant sv
-- JOIN shoes s ON sv.shoes_id = s.id
-- WHERE sv.stock > 0
-- ORDER BY s.name, sv.size, sv.color;

-- ============================================================================
-- END OF SUPABASE SQL SCRIPT
-- ============================================================================
