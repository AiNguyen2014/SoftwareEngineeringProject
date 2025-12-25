package ecommerce.shoestore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
    
    List<OrderAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<OrderAddress> findByUserIdAndIsDefaultTrue(Long userId);
    
    long countByUserId(Long userId);
}
