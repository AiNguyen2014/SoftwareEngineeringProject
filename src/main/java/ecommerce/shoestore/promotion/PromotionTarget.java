package ecommerce.shoestore.promotion;

import ecommerce.shoestore.category.Category;
import ecommerce.shoestore.shoes.Shoes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "promotiontarget")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"targetId\"")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"targetType\"", nullable = false, columnDefinition = "promotion_target_type")
    private ProductTargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"shoeId\"")
    private Shoes shoe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"categoryId\"")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"campaignId\"", nullable = false)
    private PromotionCampaign campaign;
}
