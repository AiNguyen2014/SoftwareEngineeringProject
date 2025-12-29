package ecommerce.shoestore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreateAtDesc(Long userId);
    
    long countByStatus(OrderStatus status);
}
