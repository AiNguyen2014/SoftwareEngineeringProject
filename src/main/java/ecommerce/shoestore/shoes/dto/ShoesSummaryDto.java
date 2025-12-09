package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO cho Product List View
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoesSummaryDto {
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price; // basePrice
    private String thumbnailUrl;
    private boolean outOfStock;
    private String type; // FOR_MALE, FOR_FEMALE, FOR_UNISEX
}