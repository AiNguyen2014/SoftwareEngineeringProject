package ecommerce.shoestore.promotion;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"voucherId\"")
    private Long voucherId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"discountType\"", nullable = false, columnDefinition = "voucher_discount_type")
    private VoucherDiscountType discountType;

    @Column(name = "\"discountValue\"", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "\"maxDiscountValue\"")
    private BigDecimal maxDiscountValue;

    @Builder.Default
    @Column(name = "\"minOrderValue\"", nullable = false)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "\"startDate\"", nullable = false)
    private LocalDate startDate;

    @Column(name = "\"endDate\"", nullable = false)
    private LocalDate endDate;

    @Column(name = "\"maxRedeemPerCustomer\"")
    private Long maxRedeemPerCustomer;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
    
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", length = 20, columnDefinition = "voucher_status")
    private VoucherStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"campaignId\"", nullable = false)
    private PromotionCampaign campaign;
    
    /**
     * Tự động cập nhật status dựa trên enabled và thời gian
     */
    public void updateStatus() {
        if (!Boolean.TRUE.equals(this.enabled)) {
            this.status = VoucherStatus.CANCELLED;
            return;
        }
        
        LocalDate today = LocalDate.now();
        if (today.isBefore(this.startDate)) {
            this.status = VoucherStatus.DRAFT;
        } else if (today.isAfter(this.endDate)) {
            this.status = VoucherStatus.ENDED;
        } else {
            this.status = VoucherStatus.ACTIVE;
        }
    }
    
    @PrePersist
    @PreUpdate
    protected void onSaveOrUpdate() {
        updateStatus();
    }
}
