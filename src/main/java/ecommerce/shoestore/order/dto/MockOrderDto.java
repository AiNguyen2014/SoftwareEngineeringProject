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
            case "WAITING_PAYMENT": return "Chờ thanh toán";
            case "PAID": return "Đã thanh toán";
            case "WAITING_CONFIRMATION": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "PACKING": return "Đang đóng gói";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETE_DELIVERY": return "Giao hàng thành công";
            case "CANCELED": return "Đã hủy";
            case "REQUEST_REFUND": return "Yêu cầu hoàn tiền";
            case "REFUND": return "Đã hoàn tiền";
            default: return status;
        }
    }
    
    public String getStatusColorClass() {
        if (status == null) return "secondary";
        
        switch (status) {
            case "WAITING_PAYMENT": return "warning";
            case "PAID": return "info";
            case "WAITING_CONFIRMATION": return "warning";
            case "CONFIRMED": return "primary";
            case "PACKING": return "info";
            case "SHIPPING": return "primary";
            case "COMPLETE_DELIVERY": return "success";
            case "CANCELED": return "danger";
            case "REQUEST_REFUND": return "warning";
            case "REFUND": return "secondary";
            default: return "secondary";
        }
    }
}