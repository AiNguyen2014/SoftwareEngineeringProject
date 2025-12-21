package ecommerce.shoestore.shoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    @Query("SELECT DISTINCT s FROM Shoes s " +
            "LEFT JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.images")
    Page<Shoes> findAll(Pageable pageable);

    @Query("SELECT s FROM Shoes s " +
            "LEFT JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.images " +
            "LEFT JOIN FETCH s.variants " +
            "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithDetails(@Param("shoeId") Long shoeId);

    @Query("SELECT DISTINCT s FROM Shoes s " +
            "LEFT JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.images " +
            "WHERE s.category.categoryId = :categoryId " +
            "AND s.shoeId <> :excludeShoeId")
    Page<Shoes> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeShoeId") Long excludeShoeId,
            Pageable pageable);

    /** search + sort */
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

}