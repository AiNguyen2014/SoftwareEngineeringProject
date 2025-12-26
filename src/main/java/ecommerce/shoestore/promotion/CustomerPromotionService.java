package ecommerce.shoestore.promotion;

import ecommerce.shoestore.order.Order;
import ecommerce.shoestore.promotion.dto.VoucherDisplayDTO;
import ecommerce.shoestore.promotion.dto.VoucherValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Service xử lý khuyến mãi cho customer (áp dụng voucher vào order)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPromotionService {

    private final VoucherRepository voucherRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final PromotionCampaignRepository promotionCampaignRepository;

    /**
     * Lấy danh sách voucher khả dụng cho customer
     * Lọc theo: enabled, thời gian hiệu lực, giá trị đơn tối thiểu, campaign enabled
     * userId có thể null (cho guest users)
     */
    @Transactional(readOnly = true)
    public List<Voucher> getAvailableVouchers(Long userId, BigDecimal orderSubTotal) {
        LocalDate today = LocalDate.now();
        
        return voucherRepository.findAllWithCampaign().stream()
                // Voucher phải enabled
                .filter(v -> Boolean.TRUE.equals(v.getEnabled()))
                // Campaign cũng phải enabled
                .filter(v -> v.getCampaign() != null && Boolean.TRUE.equals(v.getCampaign().getEnabled()))
                // Trong thời gian hiệu lực của voucher
                .filter(v -> !v.getStartDate().isAfter(today))
                .filter(v -> !v.getEndDate().isBefore(today))
                // Trong thời gian hiệu lực của campaign
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getStartDate().isAfter(today))
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getEndDate().isBefore(today))
                // Đủ giá trị đơn tối thiểu (nếu có)
                .filter(v -> v.getMinOrderValue() == null || orderSubTotal.compareTo(v.getMinOrderValue()) >= 0)
                // Cập nhật status trước khi trả về
                .peek(v -> {
                    v.updateStatus();
                    if (v.getCampaign() != null) {
                        v.getCampaign().updateStatus();
                    }
                })
                .toList();
    }

    /**
     * Lấy tất cả voucher đang hoạt động (không filter theo minOrderValue)
     * Dùng cho trang hiển thị danh sách tất cả vouchers
     */
    @Transactional(readOnly = true)
    public List<Voucher> getAllActiveVouchers() {
        LocalDate today = LocalDate.now();
        
        return voucherRepository.findAllWithCampaign().stream()
                // Voucher phải enabled
                .filter(v -> Boolean.TRUE.equals(v.getEnabled()))
                // Campaign cũng phải enabled
                .filter(v -> v.getCampaign() != null && Boolean.TRUE.equals(v.getCampaign().getEnabled()))
                // Trong thời gian hiệu lực của voucher
                .filter(v -> !v.getStartDate().isAfter(today))
                .filter(v -> !v.getEndDate().isBefore(today))
                // Trong thời gian hiệu lực của campaign
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getStartDate().isAfter(today))
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getEndDate().isBefore(today))
                // Cập nhật status trước khi trả về
                .peek(v -> {
                    v.updateStatus();
                    if (v.getCampaign() != null) {
                        v.getCampaign().updateStatus();
                    }
                })
                .toList();
    }

    /**
     * Lấy danh sách voucher để hiển thị cho customer (bao gồm cả voucher không đủ điều kiện)
     * Voucher đủ điều kiện sẽ hiển thị trước, voucher không đủ điều kiện hiển thị màu xám
     */
    @Transactional(readOnly = true)
    public List<VoucherDisplayDTO> getVouchersForDisplay(Long userId, BigDecimal orderSubTotal) {
        LocalDate today = LocalDate.now();
        
        return voucherRepository.findAllWithCampaign().stream()
                // Chỉ lấy voucher enabled và campaign enabled
                .filter(v -> Boolean.TRUE.equals(v.getEnabled()))
                .filter(v -> v.getCampaign() != null && Boolean.TRUE.equals(v.getCampaign().getEnabled()))
                // Chỉ lấy voucher trong thời gian hiệu lực (không lấy voucher đã hết hạn)
                .filter(v -> !v.getStartDate().isAfter(today))
                .filter(v -> !v.getEndDate().isBefore(today))
                // Chỉ lấy voucher trong thời gian campaign
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getStartDate().isAfter(today))
                .filter(v -> v.getCampaign() != null && !v.getCampaign().getEndDate().isBefore(today))
                // Chuyển sang DTO (bao gồm kiểm tra minOrderValue)
                .map(v -> VoucherDisplayDTO.fromVoucher(v, orderSubTotal))
                // Sắp xếp: voucher áp dụng được trước, sau đó theo giá trị giảm
                .sorted(Comparator.comparing(VoucherDisplayDTO::isApplicable).reversed()
                        .thenComparing(VoucherDisplayDTO::getDiscountValue, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * Validate voucher code và kiểm tra điều kiện áp dụng
     */
    @Transactional(readOnly = true)
    public VoucherValidationResult validateVoucher(String voucherCode, Long userId, BigDecimal orderSubTotal) {
        // Tìm voucher theo code
        Voucher voucher = voucherRepository.findByCode(voucherCode).orElse(null);
        
        if (voucher == null) {
            return VoucherValidationResult.fail("Mã voucher không tồn tại");
        }

        // Kiểm tra voucher có đang enabled không
        if (!Boolean.TRUE.equals(voucher.getEnabled())) {
            return VoucherValidationResult.fail("Mã voucher không khả dụng");
        }
        
        // Kiểm tra campaign có enabled không
        if (voucher.getCampaign() != null && !Boolean.TRUE.equals(voucher.getCampaign().getEnabled())) {
            return VoucherValidationResult.fail("Chiến dịch của voucher đã bị tắt");
        }

        // Kiểm tra thời gian hiệu lực của voucher
        LocalDate today = LocalDate.now();
        if (today.isBefore(voucher.getStartDate())) {
            return VoucherValidationResult.fail("Mã voucher chưa có hiệu lực");
        }
        if (today.isAfter(voucher.getEndDate())) {
            return VoucherValidationResult.fail("Mã voucher đã hết hạn");
        }
        
        // Kiểm tra thời gian hiệu lực của campaign
        if (voucher.getCampaign() != null) {
            if (today.isBefore(voucher.getCampaign().getStartDate())) {
                return VoucherValidationResult.fail("Chiến dịch chưa bắt đầu");
            }
            if (today.isAfter(voucher.getCampaign().getEndDate())) {
                return VoucherValidationResult.fail("Chiến dịch đã kết thúc");
            }
        }

        // Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && orderSubTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return VoucherValidationResult.fail(
                    String.format("Đơn hàng phải đạt tối thiểu %,.0f₫ để áp dụng voucher này", 
                            voucher.getMinOrderValue())
            );
        }

        // Kiểm tra số lần sử dụng tối đa của user
        if (voucher.getMaxRedeemPerCustomer() != null && voucher.getMaxRedeemPerCustomer() > 0) {
            long usedCount = orderVoucherRepository.countByVoucher_VoucherIdAndUserId(voucher.getVoucherId(), userId);
            if (usedCount >= voucher.getMaxRedeemPerCustomer()) {
                return VoucherValidationResult.fail("Bạn đã sử dụng hết lượt áp dụng voucher này");
            }
        }

        // Tính toán số tiền giảm giá
        BigDecimal discountAmount = calculateDiscount(voucher, orderSubTotal);

        return VoucherValidationResult.success(voucher, discountAmount);
    }

    /**
     * Tính toán số tiền giảm giá từ voucher
     */
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderSubTotal) {
        BigDecimal discountAmount;

        if (voucher.getDiscountType() == VoucherDiscountType.PERCENT) {
            // Giảm theo phần trăm
            discountAmount = orderSubTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // Áp dụng giảm tối đa nếu có
            if (voucher.getMaxDiscountValue() != null && discountAmount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                discountAmount = voucher.getMaxDiscountValue();
            }
        } else {
            // Giảm cố định (FIXED_AMOUNT)
            discountAmount = voucher.getDiscountValue();
        }

        // Đảm bảo không giảm quá giá trị đơn hàng
        if (discountAmount.compareTo(orderSubTotal) > 0) {
            discountAmount = orderSubTotal;
        }

        return discountAmount;
    }

    /**
     * Áp dụng voucher vào order (sau khi order đã được tạo)
     */
    @Transactional
    public void applyVoucherToOrder(Order order, Voucher voucher, Long userId) {
        // Kiểm tra lại voucher có hợp lệ không
        VoucherValidationResult validation = validateVoucher(
                voucher.getCode(), 
                userId, 
                order.getSubTotal()
        );

        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getErrorMessage());
        }

        // Tạo bản ghi OrderVoucher
        OrderVoucher orderVoucher = OrderVoucher.builder()
                .orderId(order.getOrderId())
                .voucher(voucher)
                .userId(userId)
                .appliedAmount(validation.getDiscountAmount())
                .build();

        orderVoucherRepository.save(orderVoucher);
        log.info("Applied voucher {} to order {} with discount {}", 
                voucher.getCode(), order.getOrderId(), validation.getDiscountAmount());
    }

    /**
     * Kiểm tra user đã dùng voucher này bao nhiêu lần
     */
    @Transactional(readOnly = true)
    public long countVoucherUsage(Long voucherId, Long userId) {
        return orderVoucherRepository.countByVoucher_VoucherIdAndUserId(voucherId, userId);
    }
    
    /**
     * Lấy danh sách campaign đang hoạt động cho sản phẩm cụ thể
     */
    @Transactional(readOnly = true)
    public List<PromotionCampaign> getActiveCampaignsForProduct(Long shoeId, Long categoryId) {
        LocalDate today = LocalDate.now();
        List<PromotionCampaign> campaigns = promotionCampaignRepository.findActiveCampaignsForProduct(
                shoeId, categoryId, today);
        
        // Update status cho mỗi campaign
        campaigns.forEach(PromotionCampaign::updateStatus);
        
        return campaigns.stream()
                .filter(c -> c.getStatus() == PromotionCampaignStatus.ACTIVE)
                .toList();
    }
    
    /**
     * Lấy tất cả campaign đang hoạt động
     */
    @Transactional(readOnly = true)
    public List<PromotionCampaign> getAllActiveCampaigns() {
        LocalDate today = LocalDate.now();
        List<PromotionCampaign> campaigns = promotionCampaignRepository.findAllActiveCampaigns(today);
        
        campaigns.forEach(PromotionCampaign::updateStatus);
        
        return campaigns.stream()
                .filter(c -> c.getStatus() == PromotionCampaignStatus.ACTIVE)
                .toList();
    }
}
