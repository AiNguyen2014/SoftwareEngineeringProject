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
    
    @Column(name = "\"orderAddressId\"")
    private Long orderAddressId;
    
    @Column(name = "\"recipientEmail\"")
    private String recipientEmail;
    
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
    //Mới sửa lại trạng kiểu string thành enum cho OrderStatus
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @org.hibernate.annotations.ColumnTransformer(
        write = "CAST(? AS order_status)"  // Ép kiểu String thành order_status khi lưu
    )
    private OrderStatus status;
    
    @Column(name = "\"createAt\"", nullable = false)
    private LocalDateTime createAt;
    
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING; //mới sửa lại trạng thái mặc định
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (shippingFee == null) {
            shippingFee = new BigDecimal("30000");
        }
    }
}
