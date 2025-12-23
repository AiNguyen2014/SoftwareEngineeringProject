package ecommerce.shoestore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingLogRepository extends JpaRepository<OrderTrackingLog, Long> {
    
    // Lấy tất cả tracking log của một đơn hàng
    @Query("SELECT log FROM OrderTrackingLog log WHERE log.orderId = :orderId ORDER BY log.changeAt ASC")
    List<OrderTrackingLog> findByOrderId(@Param("orderId") Long orderId);
    
    // Lấy tracking log mới nhất của một đơn hàng
    @Query("SELECT log FROM OrderTrackingLog log WHERE log.orderId = :orderId ORDER BY log.changeAt DESC")
    List<OrderTrackingLog> findByOrderIdOrderByChangeAtDesc(@Param("orderId") Long orderId);
    
    // Tìm tracking log theo người thực hiện thay đổi
    @Query("SELECT log FROM OrderTrackingLog log WHERE log.changedBy = :changedBy ORDER BY log.changeAt DESC")
    List<OrderTrackingLog> findByChangedBy(@Param("changedBy") String changedBy);
}