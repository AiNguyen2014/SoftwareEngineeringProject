package ecommerce.shoestore.promotion.dto;

import ecommerce.shoestore.promotion.VoucherDiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VoucherForm {
    private Long voucherId;

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    private String title;

    private String description;

    @NotNull(message = "Loại giảm không được để trống")
    private VoucherDiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải > 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountValue;

    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0", message = "Đơn tối thiểu phải >= 0")
    private BigDecimal minOrderValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private Long maxRedeemPerCustomer;

    private Boolean enabled = Boolean.TRUE;

    @NotNull(message = "Chiến dịch không được để trống")
    private Long campaignId;
}
