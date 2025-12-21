package ecommerce.shoestore.cart.dto;

import java.math.BigDecimal;

public record CartItemView(
        Long cartItemId,
        Long shoeId,
        Long variantId,
        String shoeName,
        String brand,
        String color,
        String size,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
        ) {

}
