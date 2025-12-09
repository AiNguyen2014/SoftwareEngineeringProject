package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * DTO cho Product Detail View
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoesDetailDto {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal basePrice;
    private String description;
    private String category;
    private String collection;
    private String type; // FOR_MALE, FOR_FEMALE, FOR_UNISEX

    private List<String> imageUrls;
    private Set<String> sizes;
    private Set<String> colors;
    private Integer totalStock;

    private List<ShoesSummaryDto> relatedProducts;
}
