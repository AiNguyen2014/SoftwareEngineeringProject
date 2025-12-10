package ecommerce.shoestore.shoes;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ShoesVariant - ĐÚNG THEO CLASS DIAGRAM
 */
@Entity
@Table(name = "shoes_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoesVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variant_id")
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    private Color color;

    @Column(name = "stock")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoes_id", nullable = false)
    private Shoes shoes;

    @PrePersist
    protected void onCreate() {
        if (variantId == null) {
            variantId = id;
        }
    }

    public String getSizeValue() {
        return size != null ? size.getValue() : null;
    }

    public String getColorValue() {
        return color != null ? color.name() : null;
    }
}