package ecommerce.shoestore.promotion.dto;

import ecommerce.shoestore.promotion.Voucher;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Kết quả validate voucher
 */
@Getter
@AllArgsConstructor
public class VoucherValidationResult {
    private boolean valid;
    private String errorMessage;
    private Voucher voucher;
    private BigDecimal discountAmount;

    public static VoucherValidationResult success(Voucher voucher, BigDecimal discountAmount) {
        return new VoucherValidationResult(true, null, voucher, discountAmount);
    }

    public static VoucherValidationResult fail(String errorMessage) {
        return new VoucherValidationResult(false, errorMessage, null, null);
    }
}
