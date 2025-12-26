package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCampaign_CampaignId(Long campaignId);
    
    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.campaign")
    List<Voucher> findAllWithCampaign();
    
    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.campaign WHERE v.voucherId = :id")
    Optional<Voucher> findByIdWithCampaign(@Param("id") Long id);
    
    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.campaign WHERE v.campaign.campaignId = :campaignId")
    List<Voucher> findByCampaign_CampaignId(@Param("campaignId") Long campaignId);
}
