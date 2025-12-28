package ecommerce.shoestore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Plain DTO cho mock data - KHÔNG phải Entity, không cần DB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockOrderDto {
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime createAt;
    private String status;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String note;
    private List<MockTrackingLogDto> trackingLogs;
    
    // Helper methods for display
    public String getStatusDisplay() {
        if (status == null) return "";
        
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Giao hàng thành công";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
    
    public String getStatusColorClass() {
        if (status == null) return "secondary";
        
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Giao hàng thành công";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}