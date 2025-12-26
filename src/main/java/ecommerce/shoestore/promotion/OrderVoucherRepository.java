package ecommerce.shoestore.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, Long> {
    boolean existsByVoucher_VoucherId(Long voucherId);
    long countByVoucher_VoucherIdAndUserId(Long voucherId, Long userId);
}
