package ecommerce.shoestore.shoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository chính cho Shoes - chỉ chứa các query cơ bản
 * Các query search/filter/sort đã được tách ra ShoesSearchRepository
 */
@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    /**
     * Lấy danh sách ID giày có phân trang
     */
    @Query("SELECT s FROM Shoes s")
    Page<Shoes> findAllPaged(Pageable pageable);

    /**
     * Lấy danh sách giày theo IDs kèm images (chỉ fetch images, không fetch variants để tránh lặp)
     */
    @Query("SELECT DISTINCT s FROM Shoes s "
            + "LEFT JOIN FETCH s.images "
            + "WHERE s.shoeId IN :ids")
    List<Shoes> findAllByIdsWithImages(@Param("ids") List<Long> ids);

    /**
     * Lấy chi tiết giày theo ID (cho trang chi tiết) - fetch riêng từng collection
     */
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.category " +
           "LEFT JOIN FETCH s.images " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithImages(@Param("shoeId") Long shoeId);

    /**
     * Lấy chi tiết giày kèm variants
     */
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.variants " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithVariants(@Param("shoeId") Long shoeId);

    /**
     * Lấy sản phẩm liên quan (cùng category)
     */
    @Query("SELECT DISTINCT s FROM Shoes s "
            + "LEFT JOIN FETCH s.images "
            + "WHERE s.category.categoryId = :categoryId "
            + "AND s.shoeId != :excludeShoeId "
            + "AND s.status = true")
    List<Shoes> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeShoeId") Long excludeShoeId,
            Pageable pageable);
}
