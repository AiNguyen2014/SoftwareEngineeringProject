package ecommerce.shoestore.shoesvariant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoesVariantRepository extends JpaRepository<ShoesVariant, Long> {

    // Tính tổng tồn kho của 1 sản phẩm
    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ShoesVariant v WHERE v.shoes.shoeId = :shoeId")
    Integer getTotalStockByShoeId(@Param("shoeId") Long shoeId);
}