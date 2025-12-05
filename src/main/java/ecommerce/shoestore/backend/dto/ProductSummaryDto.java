package ecommerce.shoestore.backend.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for Product List View
 * Chỉ chứa thông tin cơ bản để hiển thị trong danh sách
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDto {

    private Long id;

    private String name;

    private BigDecimal price;

    private String thumbnailUrl;

    private boolean outOfStock;

    private boolean isNew;
}