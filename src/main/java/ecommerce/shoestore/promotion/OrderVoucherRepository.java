package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, Long> {
    // TODO: Uncomment khi module Order được implement
    // boolean existsByVoucher_VoucherId(Long voucherId);
    
    // TODO: Các method này dùng khi có Order module
    // List<OrderVoucher> findByOrderId(Long orderId);
    // void deleteByOrderId(Long orderId);
}
