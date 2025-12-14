# 📊 PlantUML Diagrams - WebShoe Project

## 📁 File Diagrams

### **1. View Product List - Sequence Diagram**
📄 **File:** `01_ViewShoeList_Sequence.puml`

**Nội dung:**
- User request GET / (homepage)
- Controller processing
- Service business logic
- Repository queries
- Database interactions
- Response rendering

**Highlights:**
- ✅ Batch stock loading (getAllStocksByIds)
- ✅ JOIN FETCH optimization
- ✅ Loop with in-memory Map lookups
- 📊 Performance: 2 queries (85% reduction)

---

### **2. View Product List - Collaboration Diagram**
📄 **File:** `02_ViewShoeList_Collaboration.puml`

**Nội dung:**
- User → View → Controller → Service → Repositories → DB
- Component interactions
- Data flow relationships
- Query optimization points

**Highlights:**
- 🎯 Actor: User
- 🎨 Boundary: Thymeleaf HTML template
- 🎛️ Control: Spring Controller
- 📚 Entity: Spring Repositories
- 🔌 Database: PostgreSQL

---

### **3. View Product Detail - Sequence Diagram**
📄 **File:** `03_ViewShoeDetail_Sequence.puml`

**Nội dung:**
- User click product
- Controller fetch detail
- Service convert data
- Related products loading
- All relations fetched

**Highlights:**
- ✅ Main product with JOIN FETCH
- ✅ Error handling (NotFoundException)
- ✅ Related products batch loading
- ✅ In-memory stock calculation
- 📊 Performance: 3 queries (50% reduction)

---

### **4. View Product Detail - Collaboration Diagram**
📄 **File:** `04_ViewShoeDetail_Collaboration.puml`

**Nội dung:**
- Product detail component interactions
- Related products fetching
- Batch stock loading
- View rendering

**Highlights:**
- Complete data flow
- Optimization points
- Design patterns used
- Performance metrics

---

### **5. Complete Data Flow Architecture**
📄 **File:** `05_DataFlow_Complete.puml`

**Nội dung:**
- Overall system architecture
- Layer separation
- Database schema relationships
- Service responsibilities
- Repository optimization

**Highlights:**
- 📦 Frontend Layer (HTML views)
- 🎛️ Controller Layer
- 💼 Service Layer
- 📚 Repository Layer
- 🗄️ Database Layer

---

### **6. Query Optimization Comparison**
📄 **File:** `06_QueryOptimization_Comparison.puml`

**Nội dung:**
- Before vs After comparison
- N+1 problem visualization
- Optimization techniques
- Performance metrics

**Highlights:**
- ❌ Before: 13 queries (list), 6 queries (detail)
- ✅ After: 2 queries (list), 3 queries (detail)
- 🚀 Performance improvement: 2-6.5x faster

---

## 🚀 Cách Sử Dụng

### **Cách 1: Online (Recommended)**
Paste code vào [PlantUML Online Editor](https://www.plantuml.com/plantuml/uml/)

```
1. Copy toàn bộ code từ file .puml
2. Paste vào editor
3. Click "Render" hoặc "Refresh"
4. Xem diagram
5. Export as PNG/SVG
```

### **Cách 2: VS Code Extension**
```
1. Install "PlantUML" extension
   - ID: jebbs.plantuml
   
2. Open .puml file
   
3. Alt + D để preview diagram
   
4. Right-click → Export Diagram
```

### **Cách 3: CLI Command**
```bash
# Install PlantUML
java -jar plantuml.jar 01_ViewShoeList_Sequence.puml

# Output: 01_ViewShoeList_Sequence.png
```

---

## 📖 Diagram Descriptions

### **Sequence Diagram (07_ViewShoeList_Sequence.puml)**
```
Hiển thị thứ tự các lệnh gọi từ User → Controller → Service → Repo → DB
Dễ hiểu flow thực thi từ đầu đến cuối
Chi tiết từng bước
```

**Ví dụ:**
```
1. User requests GET /
2. Controller receives request
3. Controller calls Service.getShoesList()
4. Service calls Repo.findAll()
5. Repo queries Database
6. Database returns 12 products
... (và tiếp tục)
```

---

### **Collaboration Diagram (02_ViewShoeList_Collaboration.puml)**
```
Hiển thị mối quan hệ giữa các components
Trọng tâm vào SỰ TƯƠNG TÁC giữa actors, boundaries, controls, entities
Dễ hiểu architecture
```

**Ví dụ:**
```
User ←→ View (HTML)
   ↓
   ←→ Controller
   ↓
   ←→ Service
   ↓
   ←→ Repository
   ↓
   ←→ Database
```

---

## 📊 Key Diagrams Explained

### **Sequence: Detailed Step-by-Step**

#### List Page Example:
```
autonumber  ← Automatically number steps

1. User clicks home
2. Browser requests GET /
3. ShoesController receives request
4. Controller calls ShoesService.getShoesList(page=1, size=12)
5. Service creates PageRequest(0, 12)
6. Service calls ShoesRepository.findAll(pageable)
7. Repository executes SQL with JOIN FETCH
8. Database returns Page<Shoes> (12 products with images)
9. Service calls ShoesVariantRepository.getAllStocksByIds(ids)
   ✅ OPTIMIZATION: Single batch query for all stocks!
10. Database returns Map<Long, Integer> {1→150, 2→200, ...}
11. Service loops 12 times
    - Calls convertToSummaryDto()
    - Calls isOutOfStock() using Map (NO DB CALL)
12. Service builds ShoesListDto with 12 products
13. Service returns to Controller
14. Controller adds to model: "products", "currentPage", etc.
15. Controller returns "shoes-list" view
16. Thymeleaf renders HTML with product cards
17. Browser receives HTML and displays to user
```

---

### **Collaboration: Component Interactions**

```
          ┌─────────────┐
          │    User     │
          └──────┬──────┘
                 │ clicks home
          ┌──────▼───────────────┐
          │  shoes-list.html     │
          │   (Thymeleaf View)   │
          └──────┬───────────────┘
                 │ GET /
          ┌──────▼──────────────┐
          │  ShoesController    │
          │ @GetMapping("/")    │
          └──────┬──────────────┘
                 │ call service
          ┌──────▼──────────────┐
          │  ShoesService       │
          │ getShoesList()      │
          └────┬────────────────┘
               │
          ┌────┼─────────────────┐
          │    │                 │
       ┌──▼──────────┐   ┌──────▼──────────┐
       │  Repository │   │  VariantRepository│
       │  findAll()  │   │ getAllStocksByIds()
       └──┬──────────┘   └──────┬──────────┘
          │                      │
          └──────────┬───────────┘
                     │
              ┌──────▼──────────┐
              │  PostgreSQL DB  │
              │  (2 queries)    │
              └─────────────────┘
```

---

## 💡 Best Practices

### **When to Use Sequence Diagram:**
- ✅ Show execution flow over time
- ✅ Understand step-by-step process
- ✅ Identify performance bottlenecks
- ✅ Debug complex interactions

### **When to Use Collaboration Diagram:**
- ✅ Show component relationships
- ✅ Understand system architecture
- ✅ Design new features
- ✅ Document team responsibilities

---

## 🎯 Learning Path

### **Beginner:**
1. Start with **06_QueryOptimization_Comparison.puml**
   - Easy to understand before/after
   
2. Read **05_DataFlow_Complete.puml**
   - Overall architecture overview
   
3. Study **02_ViewShoeList_Collaboration.puml**
   - Component interactions

### **Intermediate:**
1. Read **01_ViewShoeList_Sequence.puml**
   - Detailed step-by-step execution
   
2. Study **04_ViewShoeDetail_Collaboration.puml**
   - More complex interactions
   
3. Read **03_ViewShoeDetail_Sequence.puml**
   - With error handling

### **Advanced:**
1. Compare all 6 diagrams
2. Identify optimization points
3. Propose further improvements
4. Extend diagrams for new features

---

## 📝 File Descriptions

| File | Type | Complexity | Focus |
|------|------|-----------|-------|
| 01 | Sequence | High | List page execution |
| 02 | Collaboration | Medium | List page components |
| 03 | Sequence | High | Detail page execution |
| 04 | Collaboration | Medium | Detail page components |
| 05 | Component | Medium | Overall architecture |
| 06 | Comparison | Low | Before vs After |

---

## 🔧 How to Generate Diagrams

### **Option 1: PlantUML Online**
```
URL: https://www.plantuml.com/plantuml/uml/

Steps:
1. Copy code from .puml file
2. Paste in editor
3. See preview instantly
4. Export as PNG/SVG
```

### **Option 2: VS Code**
```
Install Extensions:
- PlantUML (jebbs.plantuml)
- PlantUML Previewer

Open .puml file → Alt+D to preview
```

### **Option 3: Generate PDF Report**
```bash
# Generate all diagrams as images
plantuml diagrams/*.puml -o output -tpng

# Create PDF with all diagrams
pandoc README.md diagrams/*.png -o report.pdf
```

---

## 📌 Key Takeaways

1. **Sequence Diagrams** show **WHAT happens and WHEN**
2. **Collaboration Diagrams** show **WHO talks to WHO and HOW**
3. **Both are complementary** - use together for complete understanding
4. **Optimization is visible** - before/after comparison easy to see
5. **Performance metrics** clearly documented

---

## 🚀 Next Steps

After understanding these diagrams:
1. Implement the N+1 query fixes
2. Add unit tests for each flow
3. Create performance benchmarks
4. Document API endpoints
5. Plan future feature diagrams

---

**Last Updated:** December 9, 2025  
**Format:** PlantUML  
**Compatibility:** All versions  
**License:** MIT
