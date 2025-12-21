package ecommerce.shoestore.shoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    // Lấy danh sách sản phẩm đang bán
    @Query("SELECT s FROM Shoes s WHERE s.status = true")
    Page<Shoes> findAllActive(Pageable pageable);

    // Lấy chi tiết sản phẩm với IMAGES (tách riêng để tránh Cartesian product)
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.category " +
           "LEFT JOIN FETCH s.images " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithImages(@Param("shoeId") Long shoeId);

    // Lấy chi tiết sản phẩm với VARIANTS (tách riêng để tránh Cartesian product)
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.variants " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithVariants(@Param("shoeId") Long shoeId);

    // Lấy sản phẩm liên quan (cùng category)
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.images " +
           "WHERE s.category.categoryId = :categoryId " +
           "AND s.shoeId != :excludeShoeId " +
           "AND s.status = true")
    List<Shoes> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeShoeId") Long excludeShoeId,
            Pageable pageable);
}
