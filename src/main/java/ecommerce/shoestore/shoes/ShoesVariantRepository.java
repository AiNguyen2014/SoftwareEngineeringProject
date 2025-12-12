package ecommerce.shoestore.shoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoesVariantRepository extends JpaRepository<ShoesVariant, Long> {

    // Tính tổng tồn kho
    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ShoesVariant v WHERE v.shoes.id = :shoesId")
    Integer getTotalStockByShoeId(@Param("shoesId") Long shoesId);
}