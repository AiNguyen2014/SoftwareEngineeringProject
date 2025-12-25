package ecommerce.shoestore.admin.product;

import ecommerce.shoestore.shoes.Shoes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository dành riêng cho Admin quản lý sản phẩm
 * Tách biệt khỏi ShoesRepository (dùng cho Customer)
 */
@Repository
public interface AdminShoesRepository extends JpaRepository<Shoes, Long> {

    /* =========================
       ADMIN – SEARCH & LIST
       ========================= */
    @Query("SELECT DISTINCT s FROM Shoes s " +
           "LEFT JOIN s.category c " +
           "WHERE (:keyword IS NULL OR :keyword = '' " +
           "   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(s.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR c.categoryId = :categoryId) " +
           "AND (:brand IS NULL OR :brand = '' OR LOWER(s.brand) = LOWER(:brand)) " +
           "AND (:status IS NULL OR s.status = :status)")
    Page<Shoes> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("status") Boolean status,
            Pageable pageable
    );

    /* =========================
       ADMIN – GET DETAIL FOR EDIT
       ========================= */
    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.category c " +
           "LEFT JOIN FETCH s.images " +
           "LEFT JOIN FETCH s.variants " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithAllDetails(@Param("shoeId") Long shoeId);

    /* =========================
       ADMIN – CHECK EXISTS
       ========================= */
    boolean existsByName(String name);
    
    boolean existsByNameAndShoeIdNot(String name, Long shoeId);
}