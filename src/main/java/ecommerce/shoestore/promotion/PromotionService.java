package ecommerce.shoestore.promotion;

import ecommerce.shoestore.promotion.dto.CampaignForm;
import ecommerce.shoestore.promotion.dto.VoucherForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionCampaignRepository campaignRepository;
    private final VoucherRepository voucherRepository;
    private final OrderVoucherRepository orderVoucherRepository;

    /* ===== Campaign ===== */
    @Transactional(readOnly = true)
    public List<PromotionCampaign> listCampaigns() {
        return campaignRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PromotionCampaign> searchCampaigns(String keyword, String discountType, String status, Boolean enabled) {
        List<PromotionCampaign> campaigns = campaignRepository.findAll();
        
        return campaigns.stream()
                .filter(c -> keyword == null || keyword.isBlank() || 
                        c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(c -> discountType == null || discountType.isBlank() || 
                        c.getDiscountType().name().equals(discountType))
                .filter(c -> status == null || status.isBlank() || 
                        c.getStatus().name().equals(status))
                .filter(c -> enabled == null || c.getEnabled().equals(enabled))
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionCampaign getCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
    }

    @Transactional
    public PromotionCampaign saveCampaign(@Valid CampaignForm form) {
        validateDateRange(form.getStartDate(), form.getEndDate());
        PromotionCampaign campaign = form.getCampaignId() != null
                ? getCampaign(form.getCampaignId())
                : new PromotionCampaign();

        campaign.setName(form.getName());
        campaign.setDescription(form.getDescription());
        campaign.setStartDate(form.getStartDate());
        campaign.setEndDate(form.getEndDate());
        campaign.setDiscountType(form.getDiscountType());
        campaign.setDiscountValue(form.getDiscountValue());
        campaign.setMaxDiscountAmount(form.getMaxDiscountAmount());
        campaign.setMinOrderValue(form.getMinOrderValue());
        campaign.setEnabled(form.getEnabled() != null ? form.getEnabled() : Boolean.TRUE);
        campaign.setStatus(resolveStatus(campaign.getEnabled(), campaign.getStartDate(), campaign.getEndDate()));

        PromotionCampaign saved = campaignRepository.save(campaign);
        log.info("Saved campaign {}", saved.getCampaignId());
        return saved;
    }

    @Transactional
    public void toggleCampaignEnabled(Long id) {
        PromotionCampaign c = getCampaign(id);
        c.setEnabled(!Boolean.TRUE.equals(c.getEnabled()) ? Boolean.TRUE : Boolean.FALSE);
        c.setStatus(resolveStatus(c.getEnabled(), c.getStartDate(), c.getEndDate()));
        campaignRepository.save(c);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        PromotionCampaign campaign = getCampaign(id);
        // Check if campaign has vouchers
        if (voucherRepository.existsByCampaign_CampaignId(id)) {
            throw new IllegalStateException("Chiến dịch có voucher, không thể xóa");
        }
        campaignRepository.delete(campaign);
    }

    /* ===== Voucher ===== */
    @Transactional(readOnly = true)
    public List<Voucher> listVouchers() {
        return voucherRepository.findAllWithCampaign();
    }

    @Transactional(readOnly = true)
    public List<Voucher> searchVouchers(String keyword, Long campaignId, String discountType, Boolean enabled) {
        List<Voucher> vouchers = voucherRepository.findAllWithCampaign();
        
        return vouchers.stream()
                .filter(v -> keyword == null || keyword.isBlank() || 
                        v.getCode().toLowerCase().contains(keyword.toLowerCase()) ||
                        (v.getTitle() != null && v.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                .filter(v -> campaignId == null || v.getCampaign().getCampaignId().equals(campaignId))
                .filter(v -> discountType == null || discountType.isBlank() || 
                        v.getDiscountType().name().equals(discountType))
                .filter(v -> enabled == null || v.getEnabled().equals(enabled))
                .toList();
    }

    @Transactional(readOnly = true)
    public Voucher getVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
    }

    @Transactional
    public Voucher saveVoucher(@Valid VoucherForm form) {
        validateDateRange(form.getStartDate(), form.getEndDate());
        PromotionCampaign campaign = getCampaign(form.getCampaignId());

        if (form.getVoucherId() == null && voucherRepository.existsByCode(form.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }

        Voucher voucher = form.getVoucherId() != null ? getVoucher(form.getVoucherId()) : new Voucher();

        voucher.setCode(form.getCode().trim());
        voucher.setTitle(StringUtils.hasText(form.getTitle()) ? form.getTitle().trim() : null);
        voucher.setDescription(form.getDescription());
        voucher.setDiscountType(form.getDiscountType());
        voucher.setDiscountValue(form.getDiscountValue());
        voucher.setMaxDiscountValue(form.getMaxDiscountValue());
        voucher.setMinOrderValue(form.getMinOrderValue());
        voucher.setStartDate(form.getStartDate());
        voucher.setEndDate(form.getEndDate());
        voucher.setMaxRedeemPerCustomer(form.getMaxRedeemPerCustomer());
        voucher.setEnabled(form.getEnabled() != null ? form.getEnabled() : Boolean.TRUE);
        voucher.setCampaign(campaign);

        Voucher saved = voucherRepository.save(voucher);
        log.info("Saved voucher {}", saved.getVoucherId());
        return saved;
    }

    @Transactional
    public void toggleVoucherEnabled(Long id) {
        Voucher v = getVoucher(id);
        v.setEnabled(!Boolean.TRUE.equals(v.getEnabled()) ? Boolean.TRUE : Boolean.FALSE);
        voucherRepository.save(v);
    }

    @Transactional
    public void deleteVoucher(Long id) {
        Voucher v = getVoucher(id);
        if (orderVoucherRepository.existsByVoucher_VoucherId(id)) {
            throw new IllegalStateException("Voucher đã được sử dụng, không thể xóa");
        }
        voucherRepository.delete(v);
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc phải >= ngày bắt đầu");
        }
    }

    private PromotionCampaignStatus resolveStatus(Boolean enabled, LocalDate start, LocalDate end) {
        if (!Boolean.TRUE.equals(enabled)) {
            return PromotionCampaignStatus.CANCELLED;
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(start)) {
            return PromotionCampaignStatus.DRAFT;
        }

        if (today.isAfter(end)) {
            return PromotionCampaignStatus.ENDED;
        }

        return PromotionCampaignStatus.ACTIVE;
    }
}
