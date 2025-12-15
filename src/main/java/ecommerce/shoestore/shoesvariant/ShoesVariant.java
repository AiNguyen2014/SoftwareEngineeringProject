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

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String color;

    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"shoeId\"", nullable = false)
    private Shoes shoes;

    public String getSizeValue() {
        // Trích xuất giá trị số từ size (ví dụ: "SIZE 35" -> "35", "SIZE_35" -> "35")
        if (size == null) return null;
        return size.replaceAll(".*?(\\d+).*", "$1");
    }

    public String getColorValue() {
        // Trích xuất tên màu (ví dụ: "COLOR_BLACK" -> "BLACK", "BLACK" -> "BLACK")
        if (color == null) return null;
        return color.replaceAll("^(COLOR_)?", "").replace("_", " ");
    }
}