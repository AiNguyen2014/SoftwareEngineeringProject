package ecommerce.shoestore.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "\"orderId\"")
    private Long orderId;
    
    @Column(name = "transaction_id")
    private String transactionId; // Mã giao dịch VNPay
    
    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef; // Mã tham chiếu đơn hàng
    
    @Column(name = "amount", columnDefinition = "numeric")
    private BigDecimal amount;
    
    @Column(name = "payment_method")
    private String paymentMethod; // "VNPAY"
    
    @Column(name = "status")
    private String status; // "PENDING", "SUCCESS", "FAILED", "CANCELLED"
    
    @Column(name = "bank_code")
    private String bankCode; // Mã ngân hàng
    
    @Column(name = "card_type")
    private String cardType; // Loại thẻ
    
    @Column(name = "response_code")
    private String responseCode; // Mã phản hồi từ VNPay
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = true, updatable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // Set default cho INSERT
        if (status == null) {
            status = "PENDING";
        }
    }
    
    // Xóa @PreUpdate để trigger database tự động update updated_at
}
