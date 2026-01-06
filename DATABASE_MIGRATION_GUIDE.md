# 📊 Hướng dẫn Migration Database từ Supabase sang Render

## 🎯 Tổng quan
Chuyển dữ liệu từ Supabase database sang Render PostgreSQL để tránh lỗi connection timeout.

## 📋 Bước 1: Export dữ liệu từ Supabase

### Phương pháp 1: Sử dụng Supabase Dashboard (Dễ nhất)

1. **Đăng nhập Supabase Dashboard:**
   - Vào [supabase.com](https://supabase.com)
   - Chọn project của bạn

2. **Export Schema + Data:**
   - Sidebar → **SQL Editor** 
   - Click **"New Query"**
   - Chạy lệnh để export:
   ```sql
   -- Export tất cả tables và data
   \copy (SELECT * FROM users) TO 'users.csv' DELIMITER ',' CSV HEADER;
   \copy (SELECT * FROM products) TO 'products.csv' DELIMITER ',' CSV HEADER;
   \copy (SELECT * FROM orders) TO 'orders.csv' DELIMITER ',' CSV HEADER;
   ```

3. **Hoặc dùng Backup:**
   - **Settings** → **Database** → **Backups**
   - Download file backup gần nhất

### Phương pháp 2: Sử dụng pg_dump (Advanced)

```bash
pg_dump "postgresql://postgres.qouzchgauycrjclcdfta:Shoestorewebsite@aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres" > supabase_backup.sql
```

## 📋 Bước 2: Tạo Render Database

### 1. Tạo PostgreSQL trên Render:
- Vào [Render Dashboard](https://dashboard.render.com)
- Click **"New"** → **"PostgreSQL"**
- Điền thông tin:
  ```
  Name: shoestore-db
  Database: postgres
  User: postgres  
  Region: Singapore
  Plan: Free
  ```
- Click **"Create Database"**

### 2. Lấy connection info:
- Sau khi tạo xong → **"Connect"**
- Copy:
  - **Internal Database URL**
  - **External Database URL** 
  - **PSQL Command**

## 📋 Bước 3: Deploy với Blueprint

### 1. Commit files đã update:
```bash
git add .
git commit -m "Update config for Render PostgreSQL database"
git push origin main
```

### 2. Deploy bằng Blueprint:
- **New** → **"Blueprint"**
- Connect GitHub repository
- Render sẽ tự động:
  - Tạo PostgreSQL database
  - Tạo Web Service
  - Connect database với app

### 3. Kiểm tra deployment:
- Đợi build hoàn thành
- App sẽ start với database trống
- Cần import dữ liệu

## 📋 Bước 4: Import dữ liệu vào Render

### Phương pháp 1: Sử dụng Render Console

1. **Vào Database trên Render:**
   - Render Dashboard → PostgreSQL service
   - Tab **"Connect"** → **"PSQL Command"**

2. **Chạy restore:**
   ```bash
   psql $DATABASE_URL < supabase_backup.sql
   ```

### Phương pháp 2: Sử dụng pgAdmin/DBeaver

1. **Connect đến Render DB:**
   - Host: [từ External Database URL]
   - Port: 5432
   - Database: postgres
   - Username/Password: [từ connection info]

2. **Import file SQL:**
   - Right-click database → **"Restore"**
   - Chọn file backup từ Supabase
   - Execute

## 📋 Bước 5: Test ứng dụng

### 1. Kiểm tra connection:
```bash
# Health check
curl https://your-app-name.onrender.com/actuator/health

# API endpoints
curl https://your-app-name.onrender.com/api/products
```

### 2. Verify data:
- Login vào app
- Kiểm tra products, orders, users
- Test các chức năng CRUD

## 🎯 Lưu ý quan trọng

### ✅ Ưu điểm:
- **Ổn định**: Không còn connection timeout
- **Performance**: Database cùng region với app
- **Free**: Render PostgreSQL free tier
- **Backup**: Supabase vẫn giữ làm backup

### ⚠️ Hạn chế:
- **Storage**: 1GB limit cho free tier
- **Migration**: Cần sync data manual nếu thay đổi
- **Downtime**: Vài phút khi migration

### 🔧 Backup Plan:
Nếu lỗi migration, có thể quay về Supabase:
1. Sửa environment variables
2. Point lại về Supabase connection
3. Redeploy

## 🚀 Kết quả

**Sau migration:**
- ✅ App deploy thành công trên Render
- ✅ Database connection ổn định
- ✅ Tất cả data được preserve
- ✅ Performance tốt hơn
- ✅ Supabase vẫn intact làm backup