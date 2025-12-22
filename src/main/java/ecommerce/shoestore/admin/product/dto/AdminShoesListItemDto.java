package ecommerce.shoestore.admin.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho danh sách sản phẩm trong admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminShoesListItemDto {
    private Long shoeId;
    private String name;
    private String brand;
    private String categoryName;
    private BigDecimal basePrice;
    private Boolean status; // true=đang bán, false=ngừng bán
}
