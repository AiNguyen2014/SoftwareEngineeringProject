package ecommerce.shoestore.admin.product.dto;

import ecommerce.shoestore.shoes.ShoesType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminShoesDetailDto {

    private Long shoeId;
    private String name;
    private String brand;
    private ShoesType type;
    private BigDecimal basePrice;
    private String description;
    private String collection;
    private Long categoryId;
    private String categoryName;
    private Boolean status;

    private List<ImageDto> images;
    private List<VariantDto> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageDto {
        private Long imageId;
        private String url;
        private boolean isThumbnail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantDto {
        private Long variantId;
        private String color;
        private String size;
    }
}
