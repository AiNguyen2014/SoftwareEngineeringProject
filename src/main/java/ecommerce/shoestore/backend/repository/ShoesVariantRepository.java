package ecommerce.shoestore.backend.repository;

import ecommerce.shoestore.backend.entity.ShoesVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoesVariantRepository extends JpaRepository<ShoesVariant, Long> {

    /**
     * Tìm tất cả variants của một sản phẩm
     */
    List<ShoesVariant> findByShoesId(Long shoesId);

    /**
     * Tính tổng số lượng tồn kho của một sản phẩm
     */
    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ShoesVariant v WHERE v.shoes.id = :shoesId")
    Integer getTotalStockByShoeId(@Param("shoesId") Long shoesId);
}