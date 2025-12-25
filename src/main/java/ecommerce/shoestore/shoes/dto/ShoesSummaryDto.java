package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO cho Product List View (Card hiển thị sản phẩm)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoesSummaryDto {

    private Long shoeId;

    private String name;

    private String brand;

    private BigDecimal price;

    private String thumbnailUrl;

    private boolean outOfStock;
    
    /** Sản phẩm mới (trong vòng 14 ngày) */
    private boolean isNew;
    
    private String type;
}