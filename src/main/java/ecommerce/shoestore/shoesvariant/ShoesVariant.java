package ecommerce.shoestore.shoesvariant;

import ecommerce.shoestore.shoes.Shoes;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "\"variantId\"")
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color color;

    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"shoeId\"", nullable = false)
    private Shoes shoes;

    public String getSizeValue() {
        if (size == null) return null;
        return size.getValue();
    }

    public String getColorValue() {
        if (color == null) return null;
        return color.name();
    }
}
