package ecommerce.shoestore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Plain DTO cho mock tracking log - KHÔNG phải Entity, không cần DB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTrackingLogDto {
    private Long logId;
    private Long orderId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt; // đổi từ changeAt thành changedAt để match với database
    private String changedBy;
    private String comment;
    
    // Helper methods for display
    public String getOldStatusDisplay() {
        return oldStatus != null ? getStatusDisplayName(oldStatus) : "Khởi tạo";
    }
    
    public String getNewStatusDisplay() {
        return getStatusDisplayName(newStatus);
    }
    
    private String getStatusDisplayName(String status) {
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
}