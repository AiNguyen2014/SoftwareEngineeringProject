package ecommerce.shoestore.shoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

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

    /**
     * search + sort
     */
    @Query("""
                SELECT s FROM Shoes s
                WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :kw, '%'))
            """)
    Page<Shoes> search(
            @Param("kw") String keyword,
            Pageable pageable
    );

    /**
     * Gợi ý search autocomplete
     */
    @Query("""
                SELECT s.name
                FROM Shoes s
                WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :kw, '%'))
            """)
    List<String> findSuggestions(@Param("kw") String keyword);


    @Query("""
                SELECT s FROM Shoes s
                WHERE (:kw IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :kw, '%')))
                  AND (:categoryId IS NULL OR s.category.categoryId = :categoryId)
                  AND (:brand IS NULL OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
                  AND (:type IS NULL OR s.type = :type)
                  AND (:minPrice IS NULL OR s.basePrice >= :minPrice)
                  AND (:maxPrice IS NULL OR s.basePrice <= :maxPrice)
            """)
    Page<Shoes> searchAndFilter(
            @Param("kw") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("type") ShoesType type,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );

    // lấy brand
    @Query("""
                SELECT DISTINCT s.brand
                FROM Shoes s
                WHERE (:type IS NULL OR s.type = :type)
                AND s.brand IS NOT NULL
            """)
    List<String> findDistinctBrands(@Param("type") ShoesType type);

    @Query(value = """
            SELECT s.*
            FROM shoes s
            LEFT JOIN (
                SELECT oi."shoeId", SUM(oi.quantity) AS total_sold
                FROM orderitem oi
                JOIN "order" o ON oi."orderId" = o."orderId"
                WHERE o.status IN ('CONFIRMED', 'SHIPPING', 'COMPLETED')
                GROUP BY oi."shoeId"
            ) sold ON s."shoeId" = sold."shoeId"
            WHERE (:keyword IS NULL 
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (COALESCE(:categoryId, s."categoryId") = s."categoryId")
              AND (:brand IS NULL OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
              AND (COALESCE(:type, s.type) = s.type)
              AND (:minPrice IS NULL OR s."basePrice" >= :minPrice)
              AND (:maxPrice IS NULL OR s."basePrice" <= :maxPrice)
            ORDER BY COALESCE(sold.total_sold, 0) DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM shoes s
                    """,
            nativeQuery = true)
    Page<Shoes> findBestSeller(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
