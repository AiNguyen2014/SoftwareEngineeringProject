# SoftwareEngineeringProject
## Ecommerce Website of Shoe Store
### Project Description 
This project is an E-Commerce web application for selling footwear products online. It provides customers with the ability to browse, search, and purchase shoes through a clean, modern, and user-friendly interface. Core online shopping features are supported, such as product viewing, product management, customer accounts, order placement, and payment processing — ensuring a smooth buying experience from start to finish.

The system is developed using Spring Boot (Java) for the backend, applying a layered architecture with Spring MVC and Spring Data JPA to ensure high performance, scalability, and a clean REST API structure. The database interaction layer is implemented through Spring Data JPA, allowing efficient access to relational databases and maintaining stable data processing across the system.

The frontend interface is built using HTML + CSS only (without JavaScript or frontend frameworks). This provides a lightweight, fast-loading, and easy-to-use interface suitable for users on multiple devices while keeping the overall system simple and efficient.

While the current implementation focuses mainly on core functionality, the system is highly scalable and can be expanded in the future to include features such as inventory management, promotions, online payment integration, delivery tracking, voucher systems, and multi-role authorization — making it suitable for medium-scale online stores and ready for real commercial deployment.

## Project Struture
```text
src/main/java/com/ecommerce/shoestore
 ├── shoes
 │   ├── Shoes.java
 │   ├── ShoesController.java
 │   ├── ShoesService.java
 │   ├── ShoesRepository.java
 │   └── dto/
 ├── order
 │   ├── Order.java
 │   ├── OrderService.java
 │   ├── OrderController.java
 │   └── OrderRepository.java
 src/main/resources/
 ├── templates/                     # HTML views hiển thị qua Controller
 │   ├── index.html                 # Trang Home
 │   ├── shoes-list.html          # Danh sách sản phẩm
 │   ├── shoes-detail.html        # Chi tiết sản phẩm
 │   ├── cart.html                  # Giỏ hàng
 │   ├── order.html                 # Trang đặt hàng
 │   └── login.html                 # Đăng nhập / đăng ký
 │
 └── static/                        # Bộ tài nguyên tĩnh (CSS + Images)
     ├── css/
     │   ├── style.css              # CSS chính cho toàn trang
     │   ├── product.css            # CSS cho trang sản phẩm (optional)
     │
     └── img/                    # Tài nguyên hình ảnh

```
---
## How to run the project 
```bash
mvn spring-boot:run
```
---
## Include Credits (Team Members)
| No. | Member Name | Role |
|---|------------------------------|-------------------|
| 1 | **Chung Thị Ái Nguyên** | Project Manager |
| 2 | **Lê Huỳnh Kiều Oanh** | Member |
| 3 | **Lê Ngô Thanh Hoa** | Member |
| 4 | **Nguyễn Thị Việt Hoa** | Member |
| 5 | **Nguyễn Thị Hồng Nhung** | Member |
| 6 | **Nguyễn Thị Ngọc Thùy** | Member |
| 7 | **Nguyễn Lê Việt Quốc** | Member |
| 8 | **Phạm Trần Quốc Bảo** | Member |
| 9 | **Nguyễn Lê Hoàng Kiệt** | Member |
| 10 | **Vương Đức Huy** | Member |
## 
---