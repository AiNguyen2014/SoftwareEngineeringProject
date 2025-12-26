package ecommerce.shoestore.category;

import ecommerce.shoestore.shoes.ShoesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

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
     * Lấy tất cả danh mục, sắp xếp theo tên
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Kiểm tra danh mục có tồn tại theo tên
     */
    boolean existsByName(String name);
}
