package ecommerce.shoestore.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryView(
        List<CartItemView> items,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal total
        ) {

}