package ecommerce.shoestore.category;

import ecommerce.shoestore.shoes.ShoesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Lấy danh sách category có sản phẩm theo type
     * Nếu type = null thì lấy tất cả category có sản phẩm
     */
    @Query("""
        SELECT DISTINCT c
        FROM Category c
        JOIN c.shoes s
        WHERE (:type IS NULL OR s.type = :type)
    """)
    List<Category> findCategoriesByShoesType(
            @Param("type") ShoesType type
    );

    /**
     * Lấy TẤT CẢ category (kể cả không có sản phẩm)
     */
    @Query("SELECT c FROM Category c ORDER BY c.name")
    List<Category> findAllCategories();

    /**
     * Lấy tất cả danh mục, sắp xếp theo tên
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Kiểm tra danh mục có tồn tại theo tên
     */
    boolean existsByName(String name);
}
