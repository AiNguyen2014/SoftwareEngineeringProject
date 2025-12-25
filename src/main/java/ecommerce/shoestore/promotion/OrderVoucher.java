package ecommerce.shoestore.promotion;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ordervoucher")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"orderVoucherId\"")
    private Long orderVoucherId;

    @Column(name = "\"orderId\"", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"voucherId\"", nullable = false)
    private Voucher voucher;

    @Column(name = "\"appliedAmount\"", nullable = false)
    private BigDecimal appliedAmount;

    @Column(name = "\"createAt\"")
    private OffsetDateTime createdAt;

    @Column(name = "\"updatedAt\"")
    private OffsetDateTime updatedAt;

    @Column(name = "\"userId\"")
    private Long userId;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
