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
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Giao hàng thành công";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}