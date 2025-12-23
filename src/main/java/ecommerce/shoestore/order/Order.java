package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "\"order\"") // Escape reserved keyword với quotes
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Long orderId;
    
    // Sử dụng userId thay vì customer object để tránh complex relationship
    @Column(name = "userid", nullable = false)
    private Long userId;
    
    @Column(name = "createat", nullable = false)
    private LocalDateTime createAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(name = "\"subTotal\"", precision = 10, scale = 2, nullable = false)
    private BigDecimal subTotal;
    
    @Column(name = "\"discountAmount\"", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "\"totalAmount\"", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    // Tạm thời comment mapping để tránh lỗi
    // @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<OrderTrackingLog> trackingLogs;
    
    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.WAITING_PAYMENT;
        }
    }
}