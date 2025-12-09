package ecommerce.shoestore.shoes;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set; // 1️⃣ Đổi import từ List sang Set
import ecommerce.shoestore.category.Category;

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

    @Column(name = "shoes_id")
    private Long shoesId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ShoesType type;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShoesImage> images;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShoesVariant> variants;

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

    public String getThumbnail() {
        if (images == null || images.isEmpty()) {
            return "https://placehold.co/400x400?text=No+Image";
        }
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .map(ShoesImage::getUrl)
                .orElse(images.iterator().next().getUrl());
    }
}