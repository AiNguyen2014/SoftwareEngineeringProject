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

    // ✅ ĐÚNG: Có variantId riêng
    @Column(name = "variant_id")
    private Long variantId;

    // ✅ ĐÚNG: size là ENUM Size (không phải String)
    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private Size size;

    // ✅ ĐÚNG: color là ENUM Color (không phải String)
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

    // ✅ QUAN TRỌNG: Helper methods để convert sang String (cho DTO)
    public String getSizeValue() {
        return size != null ? size.getValue() : null;
    }

    public String getColorValue() {
        return color != null ? color.name() : null;
    }
}