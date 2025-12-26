package ecommerce.shoestore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
<<<<<<< HEAD
    
    @Query(value = "SELECT * FROM orderitem WHERE \"orderId\" = :orderId", nativeQuery = true)
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
=======
    List<OrderItem> findByOrderId(Long orderId);


>>>>>>> 36fbf3fa5454a47e01774541a10e402bfccbb730
}
