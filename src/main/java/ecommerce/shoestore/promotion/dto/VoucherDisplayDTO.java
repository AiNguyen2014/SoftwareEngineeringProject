package ecommerce.shoestore.promotion.dto;

import ecommerce.shoestore.promotion.VoucherDiscountType;
import ecommerce.shoestore.promotion.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO để hiển thị voucher cho customer
 * Bao gồm thông tin voucher có áp dụng được không và lý do
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDisplayDTO {
    
    private Long voucherId;
    private String code;
    private String description;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private BigDecimal minOrderValue;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Campaign info
    private String campaignName;
    
    // Giới hạn lượt dùng
    private Long maxRedeemPerCustomer;
    private Long userUsageCount; // Số lần user đã sử dụng voucher này
    
    // Trạng thái có thể áp dụng
    private boolean applicable;
    private String reason; // Lý do không áp dụng được (nếu có)
    
    /**
     * Tạo DTO từ Voucher entity (không kiểm tra giới hạn lượt dùng)
     */
    public static VoucherDisplayDTO fromVoucher(Voucher voucher, BigDecimal orderSubTotal) {
        return fromVoucher(voucher, orderSubTotal, null, 0L);
    }
    
    /**
     * Tạo DTO từ Voucher entity với kiểm tra giới hạn lượt dùng
     * @param voucher Voucher entity
     * @param orderSubTotal Tổng tiền đơn hàng
     * @param maxRedeemPerCustomer Giới hạn lượt dùng của voucher (có thể null)
     * @param userUsageCount Số lần user đã dùng voucher này
     */
    public static VoucherDisplayDTO fromVoucher(Voucher voucher, BigDecimal orderSubTotal, 
                                                 Long maxRedeemPerCustomer, Long userUsageCount) {
        VoucherDisplayDTO dto = VoucherDisplayDTO.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountValue(voucher.getMaxDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .campaignName(voucher.getCampaign() != null ? voucher.getCampaign().getName() : null)
                .maxRedeemPerCustomer(maxRedeemPerCustomer)
                .userUsageCount(userUsageCount != null ? userUsageCount : 0L)
                .build();
        
        // Kiểm tra điều kiện áp dụng (bao gồm cả giới hạn lượt dùng)
        dto.checkApplicability(voucher, orderSubTotal, maxRedeemPerCustomer, userUsageCount);
        
        return dto;
    }
    
    private void checkApplicability(Voucher voucher, BigDecimal orderSubTotal, 
                                      Long maxRedeemPerCustomer, Long userUsageCount) {
        LocalDate today = LocalDate.now();
        
        // Kiểm tra voucher enabled
        if (!Boolean.TRUE.equals(voucher.getEnabled())) {
            this.applicable = false;
            this.reason = "Voucher không khả dụng";
            return;
        }
        
        // Kiểm tra campaign enabled
        if (voucher.getCampaign() != null && !Boolean.TRUE.equals(voucher.getCampaign().getEnabled())) {
            this.applicable = false;
            this.reason = "Chiến dịch đã tắt";
            return;
        }
        
        // Kiểm tra thời gian voucher
        if (today.isBefore(voucher.getStartDate())) {
            this.applicable = false;
            this.reason = "Voucher chưa có hiệu lực (từ " + voucher.getStartDate() + ")";
            return;
        }
        if (today.isAfter(voucher.getEndDate())) {
            this.applicable = false;
            this.reason = "Voucher đã hết hạn";
            return;
        }
        
        // Kiểm tra thời gian campaign
        if (voucher.getCampaign() != null) {
            if (today.isBefore(voucher.getCampaign().getStartDate())) {
                this.applicable = false;
                this.reason = "Chiến dịch chưa bắt đầu";
                return;
            }
            if (today.isAfter(voucher.getCampaign().getEndDate())) {
                this.applicable = false;
                this.reason = "Chiến dịch đã kết thúc";
                return;
            }
        }
        
        // Kiểm tra giới hạn lượt dùng của user
        if (maxRedeemPerCustomer != null && maxRedeemPerCustomer > 0 && userUsageCount != null) {
            if (userUsageCount >= maxRedeemPerCustomer) {
                this.applicable = false;
                this.reason = "Đã hết lượt sử dụng (" + userUsageCount + "/" + maxRedeemPerCustomer + " lượt)";
                return;
            }
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && orderSubTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            this.applicable = false;
            BigDecimal remaining = voucher.getMinOrderValue().subtract(orderSubTotal);
            this.reason = "Mua thêm " + formatCurrency(remaining) + " để sử dụng";
            return;
        }
        
        // Tất cả điều kiện đều thỏa mãn
        this.applicable = true;
        this.reason = null;
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f₫", amount);
    }
    
    /**
     * Lấy mô tả giảm giá cho hiển thị
     */
    public String getDiscountDisplay() {
        if (discountType == VoucherDiscountType.PERCENT) {
            String text = discountValue.stripTrailingZeros().toPlainString() + "%";
            if (maxDiscountValue != null && maxDiscountValue.compareTo(BigDecimal.ZERO) > 0) {
                text += " (tối đa " + formatCurrency(maxDiscountValue) + ")";
            }
            return text;
        } else {
            return formatCurrency(discountValue);
        }
    }
}
