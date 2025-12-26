package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionCampaignRepository extends JpaRepository<PromotionCampaign, Long> {
    
    @Query("SELECT DISTINCT c FROM PromotionCampaign c " +
           "LEFT JOIN FETCH c.targets t " +
           "LEFT JOIN FETCH t.shoe " +
           "LEFT JOIN FETCH t.category " +
           "WHERE c.campaignId = :id")
    Optional<PromotionCampaign> findByIdWithTargets(@Param("id") Long id);
    
    /**
     * Tìm các campaign đang hoạt động áp dụng cho sản phẩm cụ thể
     */
    @Query("SELECT DISTINCT c FROM PromotionCampaign c " +
           "JOIN c.targets t " +
           "WHERE c.enabled = true " +
           "AND c.startDate <= :today " +
           "AND c.endDate >= :today " +
           "AND (t.shoe.shoeId = :shoeId OR t.category.categoryId = :categoryId OR t.targetType = 'ALL')")
    List<PromotionCampaign> findActiveCampaignsForProduct(
            @Param("shoeId") Long shoeId, 
            @Param("categoryId") Long categoryId,
            @Param("today") LocalDate today);
    
    /**
     * Tìm tất cả campaign đang hoạt động
     */
    @Query("SELECT c FROM PromotionCampaign c " +
           "WHERE c.enabled = true " +
           "AND c.startDate <= :today " +
           "AND c.endDate >= :today")
    List<PromotionCampaign> findAllActiveCampaigns(@Param("today") LocalDate today);
}
