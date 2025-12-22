package ecommerce.shoestore.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Lấy tất cả danh mục, sắp xếp theo tên
     */
    List<Category> findAllByOrderByNameAsc();
    
    /**
     * Kiểm tra danh mục có tồn tại theo tên
     */
    boolean existsByName(String name);
}
