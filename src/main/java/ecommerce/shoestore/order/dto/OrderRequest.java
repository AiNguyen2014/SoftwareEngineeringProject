package ecommerce.shoestore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chứa toàn bộ thông tin cần thiết để tạo đơn hàng
 * Được sử dụng bởi Controller để truyền vào Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    // Common fields
    private Long userId;
    private Long addressId;
    private String recipientEmail;
    private String paymentMethod;
    private String note;
    private String voucherCode;
    
    // Type indicator
    private OrderType type;
    
    // For CART type
    private List<Long> selectedCartItemIds;
    
    // For BUY_NOW type
    private Long variantId;
    private Integer quantity;
    
    public enum OrderType {
        CART,
        BUY_NOW
    }
}
