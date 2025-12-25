package ecommerce.shoestore.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingLogDto {
    private Long logId;
    private Long orderId;
    private String oldStatus;
    private String newStatus;
    private String oldStatusDisplay; // Tiếng Việt
    private String newStatusDisplay; // Tiếng Việt
    private LocalDateTime changeAt; // đổi từ changedAt thành changeAt
    private String changedBy;
    private String comment;
    
    // Getter methods được generate tự động bởi Lombok
    // Field oldStatusDisplay và newStatusDisplay sẽ được set từ Service
}