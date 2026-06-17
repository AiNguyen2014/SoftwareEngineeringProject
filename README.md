# SoftwareEngineeringProject
## Ecommerce Website of Shoe Store
### 
This project is a Shoe Store E-Commerce Website developed as part of a Software Engineering course.
The system allows customers to browse footwear products, view detailed information, manage shopping carts, and place orders through a clean and intuitive web interface.

The project focuses on applying software engineering principles, including layered architecture, MVC pattern, and clean separation of concerns between backend, frontend, and data access layers.

## Project Objectives
1. Build a functional e-commerce system for selling shoes online
2. Apply Spring Boot & MVC architecture in a real project
3. Design a relational database with proper entity relationships
4. Create a lightweight and fast frontend using HTML + CSS only
5. Prepare a scalable foundation for future commercial features

## Core Feature
1. Home page with featured products

2. Product listing & product detail pages

3. Product browsing and viewing

4. Shopping cart management

5. Order placement

6. User authentication (login / register)

7. Database-driven product & order management

## Technology Stack
### Backend
Programing Language: Java
Spring Framework Ecosystem:
- Spring Boot
- Spring MVC
- Spring Data JPA
### Frontend
- HTML5 (Markup Language)
- CSS3 (Style Sheet Language)
- JavaScript (Client-side Scripting Language)
- Thymeleaf (Server-side Template Engine)
### Database
- PostgreSQL (hosted on Supabase)
## System Architecture
- The system follows a layered architecture:
- Controller Layer – Handles HTTP requests & responses
- Service Layer – Business logic
- Repository Layer – Database access using JPA
- View Layer – HTML templates rendered by Spring MVC
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
 |___ ....................
 src/main/resources/
 ├── templates/                    
 │   ├── index.html                
 │   ├── shoes-list.html         
 │   ├── shoes-detail.html       
 │   ├── cart.html                 
 │   ├── order.html               
 │   └── login.html                
 │
 └── static/                       
     ├── css/
     │   ├── style.css             
     │   ├── product.css           
     │
     └── img/                   

```
---
## Screenshots
### Homepage
<img width="1919" height="924" alt="image" src="https://github.com/user-attachments/assets/30e826c5-aeb5-443e-9217-e5d92e54f10e" />
<img width="1919" height="926" alt="image" src="https://github.com/user-attachments/assets/391c342d-cb05-4180-9556-dc48e23f32eb" />

### Product List
<img width="1918" height="923" alt="image" src="https://github.com/user-attachments/assets/3fddd1cb-03a9-4668-b052-86d53bd4cd6c" />

### Product Detail
<img width="1919" height="926" alt="image" src="https://github.com/user-attachments/assets/7fbcd955-4267-40e2-b72e-ebc39a44d4e1" />

### Cart Page
<img width="1919" height="924" alt="image" src="https://github.com/user-attachments/assets/f288fcd5-3b1c-4a4d-bb81-130b200aa539" />

### Order Page
<img width="1919" height="928" alt="image" src="https://github.com/user-attachments/assets/2e683402-ccfe-4117-9b5f-9a1895b7399a" />
<img width="1919" height="928" alt="image" src="https://github.com/user-attachments/assets/c8af9686-7c5c-4fdc-9c77-bccfd6170b5f" />
<img width="1917" height="914" alt="image" src="https://github.com/user-attachments/assets/e0f8da03-fde4-4838-b01e-414511ca33e2" />
<img width="1919" height="925" alt="image" src="https://github.com/user-attachments/assets/70f83002-6ace-4a14-870b-d0ed6b593aa4" />

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

## License
This project is developed for educational purposes as part of a university course.
--------------------------
