# Render Deployment Guide

Hướng dẫn deploy ứng dụng Spring Boot Shoe Store lên Render.

## Yêu cầu trước khi deploy

1. **GitHub Repository**: Push tất cả code lên GitHub repository
2. **Tài khoản Render**: Tạo tài khoản tại [render.com](https://render.com)

## Các file đã tạo cho deployment

### 1. Dockerfile
- Containerize ứng dụng Spring Boot với Java 21
- Sử dụng multi-stage build để tối ưu kích thước image
- Cấu hình health check và security

### 2. render.yaml
- Cấu hình Blueprint cho Render
- Định nghĩa web service (sử dụng Supabase database hiện tại)
- Environment variables và settings

### 3. application-prod.properties
- Cấu hình production environment
- Tối ưu database connection pooling
- Logging và caching configuration

## Cách deploy trên Render

### Phương pháp 1: Sử dụng Blueprint (Khuyên dùng)

1. **Commit và push code:**
   ```bash
   git add .
   git commit -m "Add Render deployment configuration"
   git push origin main
   ```

2. **Tạo Blueprint trên Render:**
   - Đăng nhập vào [Render Dashboard](https://dashboard.render.com)
   - Click "New" → "Blueprint"
   - Connect GitHub repository
   - Chọn repository chứa ứng dụng
   - Render sẽ tự động detect file `render.yaml` và tạo services

### Phương pháp 2: Manual Setup (Sử dụng Supabase Database hiện tại)

1. **Tạo Web Service:**
   - New → Web Service
   - Connect GitHub repository
   - Build Command: `docker build -t shoestore .`
   - Start Command: Để trống (sử dụng CMD trong Dockerfile)
   - Plan: Free

2. **Environment Variables:**
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&currentSchema=public
   SPRING_DATASOURCE_USERNAME=postgres.qouzchgauycrjclcdfta
   SPRING_DATASOURCE_PASSWORD=Shoestorewebsite
   SPRING_MAIL_USERNAME=webshoestore17@gmail.com
   SPRING_MAIL_PASSWORD=quziuvvngrwrjzkp
   CLOUDINARY_CLOUD_NAME=dd4v8svrk
   CLOUDINARY_API_KEY=533865834927859
   CLOUDINARY_API_SECRET=YPvKEOV7wpZ9sD3vVFcw08yS-7w
   ```

## Sau khi deploy

1. **Database Connection:**
   - Ứng dụng sẽ kết nối với Supabase database hiện tại
   - Sử dụng `spring.jpa.hibernate.ddl-auto=validate` để đảm bảo schema không thay đổi
   - Database và tables đã có sẵn, không cần migration

2. **Health Check:**
   - URL: `https://your-app-name.onrender.com/actuator/health`

3. **Logs:**
   - Xem logs trong Render Dashboard để debug

## Lưu ý quan trọng

1. **Free Plan Limitations:**
   - Service sẽ sleep sau 15 phút không hoạt động
   - Database có giới hạn 1GB storage
   - Băng thông và compute time có giới hạn

2. **Database Connection:**
   - Sử dụng connection pooling để tối ưu
   - Cấu hình timeout phù hợp

3. **Security:**
   - Không commit sensitive data vào git
   - Sử dụng environment variables cho secrets

4. **Performance:**
   - Cấu hình JVM memory phù hợp với free plan
   - Enable compression và caching

## Troubleshooting

- **Build fails**: Kiểm tra Java version và dependencies trong pom.xml
- **Database connection error**: Verify environment variables
- **Application won't start**: Check logs trong Render dashboard
- **502 Bad Gateway**: Service có thể đang starting, đợi vài phút

## URLs sau khi deploy

- **Application**: `https://shoestore-backend.onrender.com`
- **Health Check**: `https://shoestore-backend.onrender.com/actuator/health`