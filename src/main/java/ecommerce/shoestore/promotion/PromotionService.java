package ecommerce.shoestore.promotion;

import ecommerce.shoestore.category.Category;
import ecommerce.shoestore.category.CategoryRepository;
import ecommerce.shoestore.promotion.dto.CampaignForm;
import ecommerce.shoestore.promotion.dto.VoucherForm;
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionCampaignRepository campaignRepository;
    private final VoucherRepository voucherRepository;
    private final PromotionTargetRepository promotionTargetRepository;
    private final ShoesRepository shoesRepository;
    private final CategoryRepository categoryRepository;

    /* ===== Campaign ===== */
    @Transactional(readOnly = true)
    public List<PromotionCampaign> listCampaigns() {
        List<PromotionCampaign> campaigns = campaignRepository.findAll();
        // Cập nhật status dựa trên thời gian hiện tại
        campaigns.forEach(PromotionCampaign::updateStatus);
        return campaigns;
    }

    @Transactional(readOnly = true)
    public List<PromotionCampaign> searchCampaigns(String keyword, String discountType, String status, Boolean enabled) {
        List<PromotionCampaign> campaigns = campaignRepository.findAll();
        // Cập nhật status dựa trên thời gian hiện tại trước khi filter
        campaigns.forEach(PromotionCampaign::updateStatus);
        
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
        PromotionCampaign campaign = campaignRepository.findByIdWithTargets(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chiến dịch"));
        // Cập nhật status dựa trên thời gian hiện tại
        campaign.updateStatus();
        return campaign;
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
        // Status sẽ được tự động set bởi @PrePersist/@PreUpdate

        PromotionCampaign saved = campaignRepository.save(campaign);

        // Xử lý targetType - ưu tiên giá trị từ form
        ProductTargetType targetType = form.getTargetType();
        log.info("Processing targets - form targetType: {}, shoeIds: {}, categoryIds: {}", 
                 targetType, form.getShoeIds(), form.getCategoryIds());
        
        // Nếu targetType là null hoặc không hợp lệ, xác định dựa trên dữ liệu
        if (targetType == null) {
            if (form.getShoeIds() != null && !form.getShoeIds().isEmpty()) {
                targetType = ProductTargetType.PRODUCT;
            } else if (form.getCategoryIds() != null && !form.getCategoryIds().isEmpty()) {
                targetType = ProductTargetType.CATEGORY;
            } else {
                targetType = ProductTargetType.ALL;
            }
            log.info("Determined targetType from data: {}", targetType);
        }
        
        // Validate: nếu chọn PRODUCT nhưng không có shoeIds -> lỗi
        if (targetType == ProductTargetType.PRODUCT && 
            (form.getShoeIds() == null || form.getShoeIds().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm khi chọn phạm vi 'Sản phẩm cụ thể'");
        }
        
        // Validate: nếu chọn CATEGORY nhưng không có categoryIds -> lỗi
        if (targetType == ProductTargetType.CATEGORY && 
            (form.getCategoryIds() == null || form.getCategoryIds().isEmpty())) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một danh mục khi chọn phạm vi 'Theo danh mục'");
        }

        // Save targets
        saveTargets(saved, targetType, form.getShoeIds(), form.getCategoryIds());
        
        // Nếu đây là update (có campaignId), kiểm tra và điều chỉnh vouchers nếu dates thay đổi
        if (form.getCampaignId() != null) {
            validateAndAdjustVouchersForCampaignDateChange(saved);
        }
        
        log.info("Saved campaign {} with targetType: {}", saved.getCampaignId(), targetType);
        return saved;
    }
    
    @Transactional
    public void saveTargets(PromotionCampaign campaign, ProductTargetType targetType, List<Long> shoeIds, List<Long> categoryIds) {
        Long campaignId = campaign.getCampaignId();
        
        // Delete existing targets và flush để đảm bảo xóa trước khi insert
        promotionTargetRepository.deleteByCampaignId(campaignId);
        promotionTargetRepository.flush();
        
        log.info("Deleted old targets for campaign {}, now saving new targets with type: {}", campaignId, targetType);
        
        if (targetType == ProductTargetType.ALL) {
            PromotionTarget target = PromotionTarget.builder()
                    .targetType(ProductTargetType.ALL)
                    .campaign(campaign)
                    .build();
            promotionTargetRepository.save(target);
            log.info("Saved ALL target for campaign {}", campaignId);
        } else if (targetType == ProductTargetType.PRODUCT && shoeIds != null && !shoeIds.isEmpty()) {
            for (Long shoeId : shoeIds) {
                Shoes shoe = shoesRepository.findById(shoeId).orElse(null);
                if (shoe != null) {
                    PromotionTarget target = PromotionTarget.builder()
                            .targetType(ProductTargetType.PRODUCT)
                            .shoe(shoe)
                            .campaign(campaign)
                            .build();
                    promotionTargetRepository.save(target);
                }
            }
            log.info("Saved {} PRODUCT targets for campaign {}", shoeIds.size(), campaignId);
        } else if (targetType == ProductTargetType.CATEGORY && categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category != null) {
                    PromotionTarget target = PromotionTarget.builder()
                            .targetType(ProductTargetType.CATEGORY)
                            .category(category)
                            .campaign(campaign)
                            .build();
                    promotionTargetRepository.save(target);
                }
            }
            log.info("Saved {} CATEGORY targets for campaign {}", categoryIds.size(), campaignId);
        }
    }

    @Transactional
    public void toggleCampaignEnabled(Long id) {
        PromotionCampaign c = getCampaign(id);
        c.setEnabled(!Boolean.TRUE.equals(c.getEnabled()) ? Boolean.TRUE : Boolean.FALSE);
        // Status sẽ được tự động set bởi @PreUpdate
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
        List<Voucher> vouchers = voucherRepository.findAllWithCampaign();
        // Cập nhật status dựa trên thời gian hiện tại
        vouchers.forEach(Voucher::updateStatus);
        return vouchers;
    }

    @Transactional(readOnly = true)
    public List<Voucher> searchVouchers(String keyword, Long campaignId, String discountType, Boolean enabled) {
        List<Voucher> vouchers = voucherRepository.findAllWithCampaign();
        // Cập nhật status dựa trên thời gian hiện tại trước khi filter
        vouchers.forEach(Voucher::updateStatus);
        
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
        Voucher voucher = voucherRepository.findByIdWithCampaign(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        // Cập nhật status dựa trên thời gian hiện tại
        voucher.updateStatus();
        return voucher;
    }
    
    @Transactional(readOnly = true)
    public List<Voucher> getVouchersByCampaign(Long campaignId) {
        List<Voucher> vouchers = voucherRepository.findByCampaign_CampaignId(campaignId);
        // Cập nhật status dựa trên thời gian hiện tại
        vouchers.forEach(Voucher::updateStatus);
        return vouchers;
    }

    @Transactional
    public Voucher saveVoucher(@Valid VoucherForm form) {
        validateDateRange(form.getStartDate(), form.getEndDate());
        PromotionCampaign campaign = getCampaign(form.getCampaignId());
        
        // Validate voucher dates phải nằm trong phạm vi campaign dates
        validateVoucherDatesWithinCampaign(form.getStartDate(), form.getEndDate(), campaign);

        if (form.getVoucherId() == null && voucherRepository.existsByCode(form.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }

        Voucher voucher = form.getVoucherId() != null ? getVoucher(form.getVoucherId()) : new Voucher();

        voucher.setCode(form.getCode().trim());
        voucher.setTitle(StringUtils.hasText(form.getTitle()) ? form.getTitle().trim() : null);
        voucher.setDescription(form.getDescription());
        voucher.setDiscountType(form.getDiscountType());
        voucher.setDiscountValue(form.getDiscountValue());
        
        // Handle maxDiscountValue - use campaign value if not provided, or 0 as fallback
        BigDecimal maxDiscountValue = form.getMaxDiscountValue();
        if (maxDiscountValue == null) {
            maxDiscountValue = campaign.getMaxDiscountAmount() != null 
                ? campaign.getMaxDiscountAmount() 
                : BigDecimal.ZERO;
        }
        voucher.setMaxDiscountValue(maxDiscountValue);

        BigDecimal minOrderValue = form.getMinOrderValue();
        if (minOrderValue == null) {
            // Fallback to campaign rule or default to 0 to respect DB not-null constraint
            minOrderValue = campaign.getMinOrderValue() != null
                ? campaign.getMinOrderValue()
                : BigDecimal.ZERO;
        }
        voucher.setMinOrderValue(minOrderValue);
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
        // TODO: Uncomment khi module Order được implement
        // if (orderVoucherRepository.existsByVoucher_VoucherId(id)) {
        //     throw new IllegalStateException("Voucher đã được sử dụng, không thể xóa");
        // }
        voucherRepository.delete(v);
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc phải >= ngày bắt đầu");
        }
    }
    
    /**
     * Validate voucher dates must be within campaign date range.
     * Voucher can have shorter time range but cannot exceed campaign boundaries.
     */
    private void validateVoucherDatesWithinCampaign(LocalDate voucherStart, LocalDate voucherEnd, 
                                                     PromotionCampaign campaign) {
        if (voucherStart == null || voucherEnd == null || campaign == null) {
            return;
        }
        
        LocalDate campaignStart = campaign.getStartDate();
        LocalDate campaignEnd = campaign.getEndDate();
        
        // Voucher start date must be >= campaign start date
        if (voucherStart.isBefore(campaignStart)) {
            throw new IllegalArgumentException(
                String.format("Ngày bắt đầu voucher (%s) không được trước ngày bắt đầu chiến dịch (%s). " +
                              "Voucher chỉ có thể hoạt động trong phạm vi thời gian của chiến dịch.",
                              voucherStart, campaignStart));
        }
        
        // Voucher end date must be <= campaign end date
        if (voucherEnd.isAfter(campaignEnd)) {
            throw new IllegalArgumentException(
                String.format("Ngày kết thúc voucher (%s) không được sau ngày kết thúc chiến dịch (%s). " +
                              "Voucher chỉ có thể hoạt động trong phạm vi thời gian của chiến dịch.",
                              voucherEnd, campaignEnd));
        }
        
        // Additional: voucher start cannot be after voucher end
        if (voucherStart.isAfter(voucherEnd)) {
            throw new IllegalArgumentException("Ngày bắt đầu voucher không được sau ngày kết thúc voucher");
        }
        
        log.info("Voucher dates validated: {} to {} within campaign {} to {}",
                 voucherStart, voucherEnd, campaignStart, campaignEnd);
    }
    
    /**
     * Validate all vouchers of a campaign when campaign dates are updated.
     * Called when updating campaign to ensure existing vouchers are still valid.
     * Also updates voucher discount rules to match campaign rules.
     */
    @Transactional
    public void validateAndAdjustVouchersForCampaignDateChange(PromotionCampaign campaign) {
        List<Voucher> vouchers = voucherRepository.findByCampaign_CampaignId(campaign.getCampaignId());
        
        for (Voucher voucher : vouchers) {
            boolean needsUpdate = false;
            
            // If voucher start is before campaign start, adjust it
            if (voucher.getStartDate().isBefore(campaign.getStartDate())) {
                voucher.setStartDate(campaign.getStartDate());
                needsUpdate = true;
                log.warn("Adjusted voucher {} start date to campaign start date {}", 
                         voucher.getCode(), campaign.getStartDate());
            }
            
            // If voucher end is after campaign end, adjust it
            if (voucher.getEndDate().isAfter(campaign.getEndDate())) {
                voucher.setEndDate(campaign.getEndDate());
                needsUpdate = true;
                log.warn("Adjusted voucher {} end date to campaign end date {}", 
                         voucher.getCode(), campaign.getEndDate());
            }
            
            // Check if voucher dates are now invalid (start > end)
            if (voucher.getStartDate().isAfter(voucher.getEndDate())) {
                // Disable the voucher as it's no longer valid
                voucher.setEnabled(false);
                needsUpdate = true;
                log.warn("Disabled voucher {} as its date range became invalid", voucher.getCode());
            }
            
            // Cập nhật quy tắc giảm giá từ campaign sang voucher
            if (voucher.getDiscountType() != campaign.getDiscountType()) {
                voucher.setDiscountType(campaign.getDiscountType());
                needsUpdate = true;
                log.info("Updated voucher {} discountType to {}", voucher.getCode(), campaign.getDiscountType());
            }
            
            if (campaign.getDiscountValue() != null && 
                !campaign.getDiscountValue().equals(voucher.getDiscountValue())) {
                voucher.setDiscountValue(campaign.getDiscountValue());
                needsUpdate = true;
                log.info("Updated voucher {} discountValue to {}", voucher.getCode(), campaign.getDiscountValue());
            }
            
            // Cập nhật maxDiscountValue
            if (campaign.getMaxDiscountAmount() != null) {
                if (!campaign.getMaxDiscountAmount().equals(voucher.getMaxDiscountValue())) {
                    voucher.setMaxDiscountValue(campaign.getMaxDiscountAmount());
                    needsUpdate = true;
                    log.info("Updated voucher {} maxDiscountValue to {}", voucher.getCode(), campaign.getMaxDiscountAmount());
                }
            }
            
            // Cập nhật minOrderValue
            if (campaign.getMinOrderValue() != null) {
                if (!campaign.getMinOrderValue().equals(voucher.getMinOrderValue())) {
                    voucher.setMinOrderValue(campaign.getMinOrderValue());
                    needsUpdate = true;
                    log.info("Updated voucher {} minOrderValue to {}", voucher.getCode(), campaign.getMinOrderValue());
                }
            }
            
            if (needsUpdate) {
                voucherRepository.save(voucher);
            }
        }
    }
}
