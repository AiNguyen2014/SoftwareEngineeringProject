package ecommerce.shoestore.promotion.dto;

import ecommerce.shoestore.promotion.ProductTargetType;
import ecommerce.shoestore.promotion.PromotionCampaignStatus;
import ecommerce.shoestore.promotion.VoucherDiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CampaignForm {
    private Long campaignId;

    @NotBlank(message = "Tên chiến dịch không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Loại giảm giá không được để trống")
    private VoucherDiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải > 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderValue;

    private PromotionCampaignStatus status;

    private Boolean enabled = Boolean.TRUE;
    
    // Target fields
    private ProductTargetType targetType = ProductTargetType.ALL;
    private List<Long> shoeIds;
    private List<Long> categoryIds;
}
