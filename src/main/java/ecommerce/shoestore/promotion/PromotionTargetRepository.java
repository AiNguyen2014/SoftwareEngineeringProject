package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, Long> {
    
    List<PromotionTarget> findByCampaign_CampaignId(Long campaignId);
    
    void deleteByCampaign_CampaignId(Long campaignId);
    
    @Query("SELECT pt FROM PromotionTarget pt " +
           "LEFT JOIN FETCH pt.shoe " +
           "LEFT JOIN FETCH pt.category " +
           "WHERE pt.campaign.campaignId = :campaignId")
    List<PromotionTarget> findWithDetailsByCampaignId(@Param("campaignId") Long campaignId);
}
