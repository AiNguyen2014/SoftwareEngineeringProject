package ecommerce.shoestore.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String category;
    private String brand;

    // THÊM DÒNG NÀY
    private Boolean isNew;

    private List<String> imageUrls;
    private Set<String> sizes;
    private Set<String> colors;
    private Integer totalStock;
    private List<ProductSummaryDto> relatedProducts;
}