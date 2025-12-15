package ecommerce.shoestore.shoes;

import ecommerce.shoestore.category.Category;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShoesType type;

    @Column(name = "\"basePrice\"", nullable = false)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"categoryId\"")
    private Category category;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShoesImage> images;

    @OneToMany(mappedBy = "shoes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShoesVariant> variants;
}