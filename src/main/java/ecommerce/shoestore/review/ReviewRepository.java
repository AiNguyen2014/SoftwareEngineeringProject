package ecommerce.shoestore.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByShoes_ShoeId(Long shoeId);

    boolean existsByOrderItem_OrderItemId(Long orderItemId);
}