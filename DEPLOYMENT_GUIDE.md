# 🚀 HƯỚNG DẪN DEPLOY ỨNG DỤNG WEBSHOE

## 📋 Mục Lục
1. [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
2. [Deploy lên Railway (Khuyến nghị)](#-cách-1-deploy-lên-railway-miễn-phí)
3. [Deploy lên Render](#-cách-2-deploy-lên-render-miễn-phí)
4. [Deploy lên Heroku](#-cách-3-deploy-lên-heroku)
5. [Deploy lên VPS (Ubuntu)](#-cách-4-deploy-lên-vps-ubuntu)
6. [Deploy bằng Docker](#-cách-5-deploy-bằng-docker)
7. [Cấu hình Environment Variables](#-cấu-hình-environment-variables)

---

## 📦 Yêu Cầu Hệ Thống

| Yêu cầu | Phiên bản |
|---------|-----------|
| Java | 21+ |
| Maven | 3.9+ |
| PostgreSQL | 14+ (đã có Supabase) |

---

## ☁️ CÁCH 1: Deploy lên Railway (Miễn phí)

### ✅ Ưu điểm
- Miễn phí $5/tháng credits
- Tự động build & deploy
- Hỗ trợ tốt Java/Spring Boot
- SSL miễn phí

### 📝 Các bước thực hiện

#### Bước 1: Đẩy code lên GitHub
```bash
# Khởi tạo git (nếu chưa có)
git init

# Thêm tất cả files
git add .

# Commit
git commit -m "Initial commit - WebShoe Store"

# Thêm remote (thay YOUR_USERNAME và REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Push lên GitHub
git push -u origin main
```

#### Bước 2: Tạo tài khoản Railway
1. Truy cập: https://railway.app
2. Đăng ký bằng GitHub

#### Bước 3: Tạo Project mới
1. Click **"New Project"**
2. Chọn **"Deploy from GitHub repo"**
3. Chọn repository của bạn
4. Railway sẽ tự động detect Java/Maven

#### Bước 4: Cấu hình Environment Variables
Vào **Settings > Variables**, thêm các biến sau:

```env
# Active Profile
SPRING_PROFILES_ACTIVE=prod

# Database (Supabase của bạn)
DATABASE_URL=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0
DATABASE_USERNAME=postgres.qouzchgauycrjclcdfta
DATABASE_PASSWORD=Shoestorewebsite
postgresql://postgres.qouzchgauycrjclcdfta:[YOUR-PASSWORD]@aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres
# Email
MAIL_USERNAME=webshoestore17@gmail.com
MAIL_PASSWORD=quziuvvngrwrjzkp

# Cloudinary
CLOUDINARY_CLOUD_NAME=dd4v8svrk
CLOUDINARY_API_KEY=533865834927859
CLOUDINARY_API_SECRET=YPvKEOV7wpZ9sD3vVFcw08yS-7w

# VNPay
VNPAY_TMN_CODE=YZ312VU8
VNPAY_HASH_SECRET=X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6
VNPAY_RETURN_URL=https://YOUR_RAILWAY_DOMAIN/payment/vnpay-return
```

#### Bước 5: Deploy
- Railway sẽ tự động build và deploy
- Sau khi deploy xong, vào **Settings > Domains** để lấy URL
- Cập nhật `VNPAY_RETURN_URL` với domain thực tế

---

## 🎨 CÁCH 2: Deploy lên Render (Miễn phí)

### ✅ Ưu điểm
- Hoàn toàn miễn phí (có giới hạn)
- Dễ sử dụng
- Tự động SSL

### 📝 Các bước thực hiện

#### Bước 1: Tạo tài khoản Render
1. Truy cập: https://render.com
2. Đăng ký bằng GitHub

#### Bước 2: Tạo Web Service mới
1. Click **"New +"** → **"Web Service"**
2. Connect GitHub repository
3. Cấu hình:
   - **Name**: webshoe-store
   - **Region**: Singapore
   - **Branch**: main
   - **Runtime**: Docker (hoặc Java)
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/*.jar --spring.profiles.active=prod`

#### Bước 3: Thêm Environment Variables
Giống như Railway (xem phần trên)

#### Bước 4: Deploy
Click **"Create Web Service"**

---

## 🟣 CÁCH 3: Deploy lên Heroku

### 📝 Các bước thực hiện

#### Bước 1: Cài Heroku CLI
```bash
# Windows (dùng Chocolatey)
choco install heroku-cli

# Hoặc tải từ: https://devcenter.heroku.com/articles/heroku-cli
```

#### Bước 2: Login và tạo app
```bash
# Login
heroku login

# Tạo app mới
heroku create webshoe-store

# Thêm Java buildpack
heroku buildpacks:set heroku/java
```

#### Bước 3: Cấu hình Environment Variables
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DATABASE_URL="jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0"
heroku config:set DATABASE_USERNAME=postgres.qouzchgauycrjclcdfta
heroku config:set DATABASE_PASSWORD=Shoestorewebsite
heroku config:set MAIL_USERNAME=webshoestore17@gmail.com
heroku config:set MAIL_PASSWORD=quziuvvngrwrjzkp
heroku config:set CLOUDINARY_CLOUD_NAME=dd4v8svrk
heroku config:set CLOUDINARY_API_KEY=533865834927859
heroku config:set CLOUDINARY_API_SECRET=YPvKEOV7wpZ9sD3vVFcw08yS-7w
heroku config:set VNPAY_TMN_CODE=YZ312VU8
heroku config:set VNPAY_HASH_SECRET=X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6
```
postgresql://postgres.qouzchgauycrjclcdfta:[YOUR-PASSWORD]@aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres
#### Bước 4: Deploy
```bash
git push heroku main
```

#### Bước 5: Xem logs
```bash
heroku logs --tail
```

---

## 🖥️ CÁCH 4: Deploy lên VPS (Ubuntu)

### 📝 Các bước thực hiện

#### Bước 1: Cài đặt Java 21
```bash
# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Cài Java 21
sudo apt install openjdk-21-jdk -y

# Kiểm tra
java -version
```

#### Bước 2: Cài đặt Maven
```bash
sudo apt install maven -y
mvn -version
```

#### Bước 3: Clone và Build project
```bash
# Clone từ GitHub
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO

# Build
./mvnw clean package -DskipTests
```

#### Bước 4: Tạo file Environment
```bash
sudo nano /etc/environment
```

Thêm các biến:
```env
DATABASE_URL="jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0"
DATABASE_USERNAME="postgres.qouzchgauycrjclcdfta"
DATABASE_PASSWORD="Shoestorewebsite"
MAIL_USERNAME="webshoestore17@gmail.com"
MAIL_PASSWORD="quziuvvngrwrjzkp"
CLOUDINARY_CLOUD_NAME="dd4v8svrk"
CLOUDINARY_API_KEY="533865834927859"
CLOUDINARY_API_SECRET="YPvKEOV7wpZ9sD3vVFcw08yS-7w"
VNPAY_TMN_CODE="YZ312VU8"
VNPAY_HASH_SECRET="X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6"
VNPAY_RETURN_URL="https://yourdomain.com/payment/vnpay-return"
```

#### Bước 5: Tạo Systemd Service
```bash
sudo nano /etc/systemd/system/webshoe.service
```

Nội dung:
```ini
[Unit]
Description=WebShoe Spring Boot Application
After=syslog.target network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -Xms128m -Xmx256m -jar /home/ubuntu/YOUR_REPO/target/webshoe-1.0.0.jar --spring.profiles.active=prod
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5
EnvironmentFile=/etc/environment

[Install]
WantedBy=multi-user.target
```

#### Bước 6: Khởi động Service
```bash
# Reload daemon
sudo systemctl daemon-reload

# Khởi động
sudo systemctl start webshoe

# Enable auto-start
sudo systemctl enable webshoe

# Xem status
sudo systemctl status webshoe

# Xem logs
sudo journalctl -u webshoe -f
```

#### Bước 7: Cài Nginx (Reverse Proxy)
```bash
sudo apt install nginx -y

# Tạo config
sudo nano /etc/nginx/sites-available/webshoe
```

Nội dung Nginx config:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/webshoe /etc/nginx/sites-enabled/

# Test config
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx
```

#### Bước 8: Cài SSL với Certbot
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## 🐳 CÁCH 5: Deploy bằng Docker

### Trên máy local (test)
```bash
# Build image
docker build -t webshoe:latest .

# Run container
docker run -d \
  --name webshoe \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL="jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0" \
  -e DATABASE_USERNAME="postgres.qouzchgauycrjclcdfta" \
  -e DATABASE_PASSWORD="Shoestorewebsite" \
  -e MAIL_USERNAME="webshoestore17@gmail.com" \
  -e MAIL_PASSWORD="quziuvvngrwrjzkp" \
  -e CLOUDINARY_CLOUD_NAME="dd4v8svrk" \
  -e CLOUDINARY_API_KEY="533865834927859" \
  -e CLOUDINARY_API_SECRET="YPvKEOV7wpZ9sD3vVFcw08yS-7w" \
  -e VNPAY_TMN_CODE="YZ312VU8" \
  -e VNPAY_HASH_SECRET="X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6" \
  -e VNPAY_RETURN_URL="http://localhost:8080/payment/vnpay-return" \
  webshoe:latest
```

### Dùng Docker Compose
Tạo file `docker-compose.yml`:
```yaml
version: '3.8'
services:
  webshoe:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&prepareThreshold=0
      - DATABASE_USERNAME=postgres.qouzchgauycrjclcdfta
      - DATABASE_PASSWORD=Shoestorewebsite
      - MAIL_USERNAME=webshoestore17@gmail.com
      - MAIL_PASSWORD=quziuvvngrwrjzkp
      - CLOUDINARY_CLOUD_NAME=dd4v8svrk
      - CLOUDINARY_API_KEY=533865834927859
      - CLOUDINARY_API_SECRET=YPvKEOV7wpZ9sD3vVFcw08yS-7w
      - VNPAY_TMN_CODE=YZ312VU8
      - VNPAY_HASH_SECRET=X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6
      - VNPAY_RETURN_URL=http://localhost:8080/payment/vnpay-return
    restart: unless-stopped
```

```bash
# Chạy
docker-compose up -d

# Xem logs
docker-compose logs -f
```

---

## 🔐 Cấu Hình Environment Variables

### Danh sách tất cả biến môi trường cần thiết:

| Biến | Mô tả | Ví dụ |
|------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | Profile đang dùng | `prod` |
| `DATABASE_URL` | JDBC URL đến PostgreSQL | `jdbc:postgresql://...` |
| `DATABASE_USERNAME` | Username database | `postgres.xxx` |
| `DATABASE_PASSWORD` | Password database | `***` |
| `MAIL_USERNAME` | Email gửi thông báo | `xxx@gmail.com` |
| `MAIL_PASSWORD` | App password của Gmail | `***` |
| `CLOUDINARY_CLOUD_NAME` | Cloud name Cloudinary | `dd4v8svrk` |
| `CLOUDINARY_API_KEY` | API Key Cloudinary | `533865834927859` |
| `CLOUDINARY_API_SECRET` | API Secret Cloudinary | `***` |
| `VNPAY_TMN_CODE` | Mã TMN VNPay | `YZ312VU8` |
| `VNPAY_HASH_SECRET` | Hash Secret VNPay | `***` |
| `VNPAY_RETURN_URL` | URL callback VNPay | `https://domain.com/payment/vnpay-return` |
| `PORT` | Port chạy (một số platform tự set) | `8080` |

---

## 🧪 Test Local Trước Khi Deploy

```bash
# Build project
./mvnw clean package -DskipTests

# Chạy với profile prod
java -jar target/webshoe-1.0.0.jar --spring.profiles.active=prod
```

Truy cập: http://localhost:8080

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Bảo mật
- **KHÔNG** commit file `.env` hoặc `application.properties` chứa credentials lên GitHub
- Sử dụng Environment Variables cho tất cả secrets
- Thay đổi password database và API keys cho production

### 2. VNPay Production
- Khi chuyển sang production, liên hệ VNPay để được cấp credentials thật
- Đổi URL từ `sandbox.vnpayment.vn` sang production URL

### 3. Database
- Supabase đã được sử dụng, không cần tạo thêm database
- Đảm bảo connection pool phù hợp với tier của Supabase

### 4. Monitoring
- Thêm Spring Boot Actuator để monitor
- Cấu hình logging cho production

---

## 📞 Hỗ Trợ

Nếu gặp lỗi khi deploy, kiểm tra:
1. **Build logs** - Xem lỗi compile
2. **Runtime logs** - Xem lỗi khi chạy
3. **Environment Variables** - Đảm bảo đã set đủ
4. **Database connection** - Test kết nối database

---

**🎉 Chúc bạn deploy thành công!**
