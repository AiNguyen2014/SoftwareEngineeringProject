package ecommerce.shoestore.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"paymentId\"")
    private Long paymentId;
    
    @Column(name = "\"orderId\"")
    private Long orderId;
    
    @Column(name = "amount", columnDefinition = "numeric")
    private BigDecimal amount;
    
    @Column(name = "provider")
    @org.hibernate.annotations.ColumnTransformer(
        write = "CAST(? AS payment_provider)"
    )
    private String provider; // "COD", "VNPAY"
    
    @Column(name = "currency")
    private String currency; // "VND"
    
    @Column(name = "status")
    @org.hibernate.annotations.ColumnTransformer(
        write = "CAST(? AS payment_status)"
    )
    private String status; // "PENDING", "SUCCESS", "FAILED", "CANCELLED"
    
    @Column(name = "\"transactionCode\"")
    private String transactionCode;
    
    @Column(name = "\"paidAt\"", nullable = true)
    private LocalDateTime paidAt;
    
    @Column(name = "\"changeAmount\"", columnDefinition = "numeric")
    private BigDecimal changeAmount;
    
    @Column(name = "\"cashReceived\"", columnDefinition = "numeric")
    private BigDecimal cashReceived;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "\"createAt\"")
    private LocalDateTime createAt;
    
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (currency == null) {
            currency = "VND";
        }
        // Set default paidAt to createAt for database constraint
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
    }
}
