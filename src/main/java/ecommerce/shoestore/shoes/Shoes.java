package ecommerce.shoestore.shoes;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import ecommerce.shoestore.category.Category;

/**
 * Entity Shoes - ĐÚNG 100% THEO CLASS DIAGRAM
 */
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
    private Long id;

    // ✅ ĐÚNG: Có shoesId riêng như trong diagram
    @Column(name = "shoes_id")
    private Long shoesId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 100)
    private String brand;

    // ✅ ĐÚNG: type là ENUM (FOR_FEMALE, FOR_MALE, FOR_UNISEX)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ShoesType type;

    // ✅ ĐÚNG: basePrice (không phải price)
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ✅ ĐÚNG: collection
    @Column(length = 255)
    private String collection;

    // ✅ Relationship với Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ✅ Relationship với ShoesImage (1 Shoes - nhiều Images)
    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShoesImage> images;

    // ✅ Relationship với ShoesVariant (1 Shoes - nhiều Variants)
    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShoesVariant> variants;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
        if (shoesId == null) {
            shoesId = id;
        }
    }

    // ✅ Helper method để lấy thumbnail
    public String getThumbnail() {
        if (images == null || images.isEmpty()) {
            return "https://placehold.co/400x400?text=No+Image";
        }
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .map(ShoesImage::getUrl) // ✅ GỌI getUrl()
                .orElse(images.get(0).getUrl());
    }
}