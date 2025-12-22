package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromotionCampaignRepository extends JpaRepository<PromotionCampaign, Long> {
    
    @Query("SELECT DISTINCT c FROM PromotionCampaign c " +
           "LEFT JOIN FETCH c.targets t " +
           "LEFT JOIN FETCH t.shoe " +
           "LEFT JOIN FETCH t.category " +
           "WHERE c.campaignId = :id")
    Optional<PromotionCampaign> findByIdWithTargets(@Param("id") Long id);
}
