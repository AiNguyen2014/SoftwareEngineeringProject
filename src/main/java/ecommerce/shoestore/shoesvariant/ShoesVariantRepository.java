package ecommerce.shoestore.shoesvariant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoesVariantRepository extends JpaRepository<ShoesVariant, Long> {

    /**
     * Batch load tổng tồn kho cho nhiều sản phẩm cùng lúc
     * Tránh N+1 query problem
     */
    @Query("SELECT v.shoes.shoeId, COALESCE(SUM(v.stock), 0) " +
           "FROM ShoesVariant v WHERE v.shoes.shoeId IN :shoeIds GROUP BY v.shoes.shoeId")
    List<Object[]> getTotalStocksByShoeIds(@Param("shoeIds") List<Long> shoeIds);
}