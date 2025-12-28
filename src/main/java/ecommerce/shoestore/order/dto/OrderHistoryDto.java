package ecommerce.shoestore.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ecommerce.shoestore.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDto {
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime createAt;
    private OrderStatus status;
    private String statusDisplay; // Tiếng Việt 
    private String statusColorClass; // CSS class
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String note;
    private List<OrderTrackingLogDto> trackingLogs;
    
    // Helper method for display
    public String getStatusDisplay() {
        if (status == null) return "";
        
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPING: return "Đang giao hàng";
            case COMPLETED: return "Giao hàng thành công";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
    
    public String getStatusColorClass() {
        if (status == null) return "secondary";
        
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPING: return "Đang giao hàng";
            case COMPLETED: return "Giao hàng thành công";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
}