package ecommerce.shoestore.shoesvariant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoesVariantRepository extends JpaRepository<ShoesVariant, Long> {

    // Tính tổng tồn kho của 1 sản phẩm
    @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ShoesVariant v WHERE v.shoes.shoeId = :shoeId")
    Integer getTotalStockByShoeId(@Param("shoeId") Long shoeId);
    
    // Tính tổng tồn kho của nhiều sản phẩm (batch)
    @Query("SELECT v.shoes.shoeId, SUM(v.stock) FROM ShoesVariant v WHERE v.shoes.shoeId IN :shoeIds GROUP BY v.shoes.shoeId")
    java.util.List<Object[]> getTotalStocksByShoeIds(@Param("shoeIds") java.util.List<Long> shoeIds);
    
    // Lấy variant với shoes (eager loading)
    @Query("SELECT v FROM ShoesVariant v LEFT JOIN FETCH v.shoes WHERE v.variantId = :variantId")
    java.util.Optional<ShoesVariant> findByIdWithShoes(@Param("variantId") Long variantId);
}