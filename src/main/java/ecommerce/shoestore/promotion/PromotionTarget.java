package ecommerce.shoestore.promotion;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "\"targetType\"", nullable = false)
    private PromotionTargetType targetType;

    @Column(name = "\"shoeId\"")
    private Long shoeId;

    @Column(name = "\"categoryId\"")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"campaignId\"", nullable = false)
    private PromotionCampaign campaign;
}
