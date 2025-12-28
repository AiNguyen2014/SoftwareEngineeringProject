package ecommerce.shoestore.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    Optional<PaymentTransaction> findByOrderId(Long orderId);
    
    Optional<PaymentTransaction> findByVnpTxnRef(String vnpTxnRef);
    
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    
    // Native query để update - DISABLE USER TRIGGER temporarily
    // Chỉ tắt user-defined triggers, KHÔNG tắt system triggers (FK constraints)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "ALTER TABLE payment_transactions DISABLE TRIGGER USER; " +
            "UPDATE payment_transactions SET " +
            "status = :status, " +
            "transaction_id = :transactionId, " +
            "bank_code = :bankCode, " +
            "card_type = :cardType, " +
            "response_code = :responseCode, " +
            "updated_at = NOW() " +
            "WHERE id = :id; " +
            "ALTER TABLE payment_transactions ENABLE TRIGGER USER", nativeQuery = true)
    void updatePaymentTransaction(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("transactionId") String transactionId,
            @Param("bankCode") String bankCode,
            @Param("cardType") String cardType,
            @Param("responseCode") String responseCode);
}
