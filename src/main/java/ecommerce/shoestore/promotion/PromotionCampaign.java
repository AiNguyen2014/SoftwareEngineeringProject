package ecommerce.shoestore.promotion;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotioncampaign")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"campaignId\"")
    private Long campaignId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "\"startDate\"", nullable = false)
    private LocalDate startDate;

    @Column(name = "\"endDate\"", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"discountType\"", nullable = false, columnDefinition = "voucher_discount_type")
    private VoucherDiscountType discountType;

    @Column(name = "\"discountValue\"", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "\"maxDiscountAmount\"")
    private BigDecimal maxDiscountAmount;

    @Column(name = "\"minOrderValue\"")
    private BigDecimal minOrderValue;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "promotion_campaign_status")
    private PromotionCampaignStatus status;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Builder.Default
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Voucher> vouchers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionTarget> targets = new ArrayList<>();
    
    /**
     * Tự động cập nhật status dựa trên enabled và thời gian
     */
    public void updateStatus() {
        if (!Boolean.TRUE.equals(this.enabled)) {
            this.status = PromotionCampaignStatus.CANCELLED;
            return;
        }
        
        LocalDate today = LocalDate.now();
        if (today.isBefore(this.startDate)) {
            this.status = PromotionCampaignStatus.DRAFT;
        } else if (today.isAfter(this.endDate)) {
            this.status = PromotionCampaignStatus.ENDED;
        } else {
            this.status = PromotionCampaignStatus.ACTIVE;
        }
    }
    
    @PrePersist
    @PreUpdate
    protected void onSaveOrUpdate() {
        updateStatus();
    }
}
