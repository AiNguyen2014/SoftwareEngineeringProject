package ecommerce.shoestore.shoesvariant;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoesVariantDto {

    private Long variantId;
    private String size;
    private String color;
    private Integer stock;
}
