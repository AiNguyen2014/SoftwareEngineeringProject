package ecommerce.shoestore.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"orderId\"")
    private Long orderId;
    
    @Column(name = "\"userId\"", nullable = false)
    private Long userId;
    
    @Column(name = "\"recipientName\"")
    private String recipientName;
    
    @Column(name = "\"recipientPhone\"")
    private String recipientPhone;
    
    @Column(name = "\"recipientEmail\"")
    private String recipientEmail;
    
    @Column(name = "\"recipientAddress\"", columnDefinition = "TEXT")
    private String recipientAddress;
    
    @Column(name = "\"subTotal\"", nullable = false, columnDefinition = "numeric")
    private BigDecimal subTotal;
    
    @Column(name = "\"shippingFee\"", nullable = false, columnDefinition = "numeric")
    private BigDecimal shippingFee;
    
    @Column(name = "\"discountAmount\"", nullable = false, columnDefinition = "numeric")
    private BigDecimal discountAmount;
    
    @Column(name = "\"totalAmount\"", nullable = false, columnDefinition = "numeric")
    private BigDecimal totalAmount;
    
    @Column(name = "\"paymentMethod\"", nullable = false)
    @org.hibernate.annotations.ColumnTransformer(
        write = "CAST(? AS payment_method)"
    )
    private String paymentMethod; // "COD" hoặc "TRANSFER"
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "status", nullable = false)
    @org.hibernate.annotations.ColumnTransformer(
        write = "CAST(? AS order_status)"
    )
    private String status; // "PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED"
    
    @Column(name = "\"createAt\"", nullable = false)
    private LocalDateTime createAt;
    
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (shippingFee == null) {
            shippingFee = new BigDecimal("30000");
        }
    }
}
