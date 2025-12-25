package ecommerce.shoestore.shoes;

import ecommerce.shoestore.category.Category;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "shoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shoes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"shoeId\"")
    private Long shoeId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 255)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShoesType type;

    @Column(name = "\"basePrice\"", nullable = false)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"categoryId\"")
    private Category category;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean status = true;
    
    @CreationTimestamp
    @Column(name = "\"createdAt\"", updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("isThumbnail DESC, imageId ASC")
    private List<ShoesImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ShoesVariant> variants = new HashSet<>();
}