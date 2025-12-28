package ecommerce.shoestore.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<Order, Long> {
    
    // Tìm orders theo userId
    List<Order> findByUserIdOrderByCreateAtDesc(Long userId);
    
    // Tìm đơn hàng theo user ID với paging
    Page<Order> findByUserIdOrderByCreateAtDesc(Long userId, Pageable pageable);
    
    // Tìm đơn hàng theo trạng thái  
    List<Order> findByStatusOrderByCreateAtDesc(String status);
    
    // Tìm đơn hàng trong khoảng thời gian
    List<Order> findByCreateAtBetweenOrderByCreateAtDesc(java.time.LocalDateTime startDate, 
                                                        java.time.LocalDateTime endDate);
    // Tìm theo trạng thái và phân trang
    @Query(value = "SELECT * FROM \"order\" WHERE status = CAST(:#{#status.name()} AS order_status)", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE status = CAST(:#{#status.name()} AS order_status)",
           nativeQuery = true)
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
}