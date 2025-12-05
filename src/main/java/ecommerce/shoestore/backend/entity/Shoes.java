package ecommerce.shoestore.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "shoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shoes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    private BigDecimal price;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String brand;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private BigDecimal rating;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "stock_status")
    private String stockStatus;

    // QUAN TRỌNG: Mapping chuẩn theo Diagram (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShoesImage> images;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShoesVariant> variants;
}