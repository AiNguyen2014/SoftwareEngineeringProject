package ecommerce.shoestore.shoes.crud;

import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ShoesSearchRepository extends JpaRepository<Shoes, Long> {

    /**
     * Search + Sort theo keyword (tìm theo tên hoặc brand)
     */
    @Query("""
        SELECT s FROM Shoes s
        WHERE s.name ILIKE CONCAT('%', :kw, '%')
           OR s.brand ILIKE CONCAT('%', :kw, '%')
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
        WHERE s.name ILIKE CONCAT('%', :kw, '%')
           OR s.brand ILIKE CONCAT('%', :kw, '%')
    """)
    List<String> findSuggestions(@Param("kw") String keyword);

    /**
     * Search và Filter với nhiều tiêu chí + Sort động
     */
    @Query(value = """
        SELECT *
        FROM shoes s
        WHERE (:kw IS NULL
                OR s.name ILIKE '%' || :kw || '%'
                OR s.brand ILIKE '%' || :kw || '%')
          AND (:categoryId IS NULL OR s."categoryId" = :categoryId)
          AND (:brand IS NULL OR s.brand ILIKE '%' || :brand || '%')
          AND (:type IS NULL OR s.type = CAST(:type AS varchar))
          AND (:minPrice IS NULL OR s."basePrice" >= :minPrice)
          AND (:maxPrice IS NULL OR s."basePrice" <= :maxPrice)
    
        ORDER BY
          CASE WHEN :sort = 'newest'     THEN s."createdAt" END DESC,
          CASE WHEN :sort = 'price_asc'  THEN s."basePrice" END ASC,
          CASE WHEN :sort = 'price_desc' THEN s."basePrice" END DESC,
          CASE WHEN :sort = 'name_asc'   THEN s.name END ASC,
          CASE WHEN :sort = 'name_desc'  THEN s.name END DESC,
          s."createdAt" DESC
        """,
            countQuery = """
            SELECT COUNT(*)
            FROM shoes s
            WHERE (:kw IS NULL
                    OR s.name ILIKE '%' || :kw || '%'
                    OR s.brand ILIKE '%' || :kw || '%')
              AND (:categoryId IS NULL OR s."categoryId" = :categoryId)
              AND (:brand IS NULL OR s.brand ILIKE '%' || :brand || '%')
              AND (:type IS NULL OR s.type = CAST(:type AS varchar))
              AND (:minPrice IS NULL OR s."basePrice" >= :minPrice)
              AND (:maxPrice IS NULL OR s."basePrice" <= :maxPrice)
        """,
            nativeQuery = true)
    Page<Shoes> searchAndFilter(
            @Param("kw") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("sort") String sort,
            Pageable pageable
    );

    /**
     * Lấy danh sách brands (không filter theo type)
     */
    @Query("""
        SELECT DISTINCT s.brand
        FROM Shoes s
        WHERE s.brand IS NOT NULL
    """)
    List<String> findDistinctBrands();

    /**
     * Lấy danh sách brands theo type
     */
    @Query("""
        SELECT DISTINCT s.brand
        FROM Shoes s
        WHERE s.brand IS NOT NULL
          AND s.type = :type
    """)
    List<String> findDistinctBrandsByType(@Param("type") ShoesType type);

    /**
     * Tìm sản phẩm bán chạy nhất (sort theo số lượng bán)
     */
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
