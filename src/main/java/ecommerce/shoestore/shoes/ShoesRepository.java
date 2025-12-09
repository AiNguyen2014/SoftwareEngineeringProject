package ecommerce.shoestore.shoes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {

    /**
     * Tìm tất cả với JOIN FETCH để tránh N+1 query
     */
    @Query("SELECT DISTINCT s FROM Shoes s " +
            "LEFT JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.images")
    Page<Shoes> findAll(Pageable pageable);

    /**
     * Tìm theo Category với JOIN FETCH
     */
    @Query("SELECT DISTINCT s FROM Shoes s " +
            "LEFT JOIN FETCH s.category c " +
            "LEFT JOIN FETCH s.images " +
            "WHERE c.name = :categoryName")
    Page<Shoes> findByCategory_Name(String categoryName, Pageable pageable);

    /**
     * Tìm theo ID với EAGER fetch images và variants
     */
    @Query("SELECT s FROM Shoes s " +
            "LEFT JOIN FETCH s.images " +
            "LEFT JOIN FETCH s.variants " +
            "WHERE s.id = :id")
    Optional<Shoes> findByIdWithDetails(Long id);
}