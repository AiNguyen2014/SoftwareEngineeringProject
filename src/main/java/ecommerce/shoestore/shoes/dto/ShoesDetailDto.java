package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import ecommerce.shoestore.shoesvariant.ShoesVariantDto;

/**
 * DTO cho Product Detail View (Trang chi tiết)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ShoesDetailDto {

    private Long shoeId;
    private String name;
    private String brand;
    private BigDecimal basePrice;
    private String description;
    private String category;
    private Set<String> sizes;
    private Set<String> colors;
    private String collection;
    private String type;
    private List<String> imageUrls;
    private Integer totalStock;

    private List<ShoesVariantDto> variants;

    private List<ShoesSummaryDto> relatedProducts;

}
