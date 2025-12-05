package ecommerce.shoestore.backend.repository;
import ecommerce.shoestore.backend.entity.Shoes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShoesRepository extends JpaRepository<Shoes, Long> {
    // Tìm tất cả (dùng cho trang chủ)
    Page<Shoes> findAll(Pageable pageable);

    // Tìm theo tên Category (MEN, WOMEN, KIDS)
    Page<Shoes> findByCategory_Name(String categoryName, Pageable pageable);

    @Query("SELECT s FROM Shoes s LEFT JOIN FETCH s.images WHERE s.id = :id")
    Optional<Shoes> findByIdWithImages(Long id);
}