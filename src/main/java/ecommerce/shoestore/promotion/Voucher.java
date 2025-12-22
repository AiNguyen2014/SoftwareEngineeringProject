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

    @Column(name = "\"minOrderValue\"")
    private BigDecimal minOrderValue;

    @Column(name = "\"startDate\"", nullable = false)
    private LocalDate startDate;

    @Column(name = "\"endDate\"", nullable = false)
    private LocalDate endDate;

    @Column(name = "\"maxRedeemPerCustomer\"")
    private Long maxRedeemPerCustomer;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"campaignId\"", nullable = false)
    private PromotionCampaign campaign;
}
