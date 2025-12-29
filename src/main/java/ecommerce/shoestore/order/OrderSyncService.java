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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncOrdersToTrackingLog() {
        try {
            List<Order> allOrders = orderRepository.findAll();
            
            for (Order order : allOrders) {
                List<OrderTrackingLog> existingLogs = trackingLogRepository.findByOrderIdOrderByChangeAtDesc(order.getOrderId());
                
                if (existingLogs.isEmpty()) {
                    createDefaultTrackingLog(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncSingleOrderToTrackingLog(Order order) {
        try {
            List<OrderTrackingLog> existingLogs = trackingLogRepository.findByOrderIdOrderByChangeAtDesc(order.getOrderId());
            if (existingLogs.isEmpty()) {
                createDefaultTrackingLog(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createDefaultTrackingLog(Order order) {
        OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                .orderId(order.getOrderId())
                .oldStatus("Không có")
                .newStatus(order.getStatus() != null ? order.getStatus().name() : "PENDING")
                .changedAt(order.getCreateAt() != null ? order.getCreateAt() : LocalDateTime.now())
                .changedBy("System")
                .comment(order.getNote() != null ? order.getNote() : "Không có")
                .build();
        
        trackingLogRepository.save(trackingLog);
    }
}