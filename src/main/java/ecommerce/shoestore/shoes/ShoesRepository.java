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

    // Method for pagination without fetch joins to avoid N+1 and in-memory pagination
    @Query("SELECT DISTINCT s FROM Shoes s")
    Page<Shoes> findAllPaged(Pageable pageable);

    // Method for fetching with joins (without pagination)
    @Query("SELECT DISTINCT s FROM Shoes s "
            + "LEFT JOIN FETCH s.category "
            + "LEFT JOIN FETCH s.images")
    Page<Shoes> findAll(Pageable pageable);

    // Method to get shoes with details by IDs (for post-processing after pagination)
    @Query("SELECT DISTINCT s FROM Shoes s "
            + "LEFT JOIN FETCH s.category "
            + "LEFT JOIN FETCH s.images "
            + "WHERE s.shoeId IN :shoeIds")
    java.util.List<Shoes> findAllWithDetailsByIds(@Param("shoeIds") java.util.List<Long> shoeIds);

    @Query("SELECT s FROM Shoes s " +
           "LEFT JOIN FETCH s.category " +
           "LEFT JOIN FETCH s.images " +
           "LEFT JOIN FETCH s.variants " +
           "WHERE s.shoeId = :shoeId")
    Optional<Shoes> findByIdWithDetails(@Param("shoeId") Long shoeId);

    @Query("SELECT s FROM Shoes s "
            + "LEFT JOIN FETCH s.images "
            + "WHERE s.category.categoryId = :categoryId "
            + "AND s.shoeId != :excludeShoeId "
            + "AND s.status = true")
    List<Shoes> findRelatedProducts(
            @Param("categoryId") Long categoryId,
            @Param("excludeShoeId") Long excludeShoeId,
            Pageable pageable);
}
