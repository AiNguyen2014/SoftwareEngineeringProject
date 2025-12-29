package ecommerce.shoestore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO đại diện cho dữ liệu của một OrderItem trước khi lưu vào DB
 * Giúp tách logic chuẩn bị dữ liệu ra khỏi entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemData {
    private Long shoeId;
    private Integer quantity;
    private String productName;
    private String variantInfo; // "Size: 39, Color: WHITE"
    private BigDecimal unitPrice;
    private BigDecimal shopDiscount;
    private BigDecimal itemTotal;
    
    // Để dễ cleanup sau khi order thành công
    private Long cartItemId; // Null nếu là BUY_NOW
}
