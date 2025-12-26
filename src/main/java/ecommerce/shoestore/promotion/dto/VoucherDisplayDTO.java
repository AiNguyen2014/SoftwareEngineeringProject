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
    
    // Trạng thái có thể áp dụng
    private boolean applicable;
    private String reason; // Lý do không áp dụng được (nếu có)
    
    /**
     * Tạo DTO từ Voucher entity
     */
    public static VoucherDisplayDTO fromVoucher(Voucher voucher, BigDecimal orderSubTotal) {
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
                .build();
        
        // Kiểm tra điều kiện áp dụng
        dto.checkApplicability(voucher, orderSubTotal);
        
        return dto;
    }
    
    private void checkApplicability(Voucher voucher, BigDecimal orderSubTotal) {
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
