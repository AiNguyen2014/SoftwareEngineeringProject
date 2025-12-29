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
    
    // Tìm đơn hàng theo trạng thái và thời gian cho revenue report
    @Query(value = "SELECT * FROM \"order\" WHERE status = CAST(:status AS order_status) AND \"createAt\" BETWEEN :startDate AND :endDate ORDER BY \"createAt\" DESC", 
           nativeQuery = true)
    List<Order> findByStatusAndCreateAtBetween(@Param("status") String status, 
                                              @Param("startDate") java.time.LocalDateTime startDate,
                                              @Param("endDate") java.time.LocalDateTime endDate);
    // Tìm theo trạng thái và phân trang
    @Query(value = "SELECT * FROM \"order\" WHERE status = CAST(:#{#status.name()} AS order_status)", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE status = CAST(:#{#status.name()} AS order_status)",
           nativeQuery = true)
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
    
    // Đếm số đơn hàng theo trạng thái
    @Query(value = "SELECT count(*) FROM \"order\" WHERE status = CAST(:#{#status.name()} AS order_status)", 
           nativeQuery = true)
    long countByStatus(@Param("status") OrderStatus status);
    
    // Tìm kiếm theo mã đơn hàng chính xác
    @Query(value = "SELECT * FROM \"order\" WHERE \"orderId\" = :orderId", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE \"orderId\" = :orderId",
           nativeQuery = true)
    Page<Order> findByOrderId(@Param("orderId") Long orderId, Pageable pageable);
    
    // Tìm kiếm theo mã đơn hàng chứa từ khóa
    @Query(value = "SELECT * FROM \"order\" WHERE CAST(\"orderId\" AS TEXT) LIKE %:keyword%", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE CAST(\"orderId\" AS TEXT) LIKE %:keyword%",
           nativeQuery = true)
    Page<Order> findByOrderIdContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // Tìm kiếm theo tên khách hàng (trong recipientName)
    @Query(value = "SELECT * FROM \"order\" WHERE LOWER(\"recipientName\") LIKE LOWER(CONCAT('%', :keyword, '%'))", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE LOWER(\"recipientName\") LIKE LOWER(CONCAT('%', :keyword, '%'))",
           nativeQuery = true)
    Page<Order> findByCustomerNameContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // Tìm kiếm trong cả mã đơn hàng và tên khách hàng
    @Query(value = "SELECT * FROM \"order\" WHERE " +
           "CAST(\"orderId\" AS TEXT) LIKE %:keyword% OR " +
           "LOWER(\"recipientName\") LIKE LOWER(CONCAT('%', :keyword, '%'))", 
           countQuery = "SELECT count(*) FROM \"order\" WHERE " +
           "CAST(\"orderId\" AS TEXT) LIKE %:keyword% OR " +
           "LOWER(\"recipientName\") LIKE LOWER(CONCAT('%', :keyword, '%'))",
           nativeQuery = true)
    Page<Order> findByOrderIdOrCustomerNameContaining(@Param("keyword") String keyword, Pageable pageable);
}