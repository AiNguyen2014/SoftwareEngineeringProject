package ecommerce.shoestore.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderSyncService {

    @Autowired
    private OrderHistoryRepository orderRepository;
    
    @Autowired
    private OrderTrackingLogRepository trackingLogRepository;

    /**
     * Sync dữ liệu từ Order sang OrderTrackingLog
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncOrdersToTrackingLog() {
        try {
            // Lấy tất cả orders
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("DEBUG Sync: Found " + allOrders.size() + " orders to sync");
            
            for (Order order : allOrders) {
                // Kiểm tra xem order này đã có tracking log chưa
                List<OrderTrackingLog> existingLogs = trackingLogRepository.findByOrderIdOrderByChangeAtDesc(order.getOrderId());
                
                if (existingLogs.isEmpty()) {
                    // Tạo tracking log từ order
                    OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                            .orderId(order.getOrderId())
                            .oldStatus("Không có") // Cột trống thì để "Không có"
                            .newStatus(order.getStatus() != null ? order.getStatus() : "PENDING")
                            .changedAt(order.getCreateAt() != null ? order.getCreateAt() : LocalDateTime.now())
                            .changedBy("System")
                            .comment(order.getNote() != null ? order.getNote() : "Không có")
                            .build();
                    
                    trackingLogRepository.save(trackingLog);
                    System.out.println("DEBUG Sync: Created tracking log for order: " + order.getOrderId());
                } else {
                    System.out.println("DEBUG Sync: Order " + order.getOrderId() + " already has " + existingLogs.size() + " tracking logs");
                }
            }
            System.out.println("DEBUG Sync: Completed sync operation");
        } catch (Exception e) {
            System.err.println("ERROR Sync: Failed to sync orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}