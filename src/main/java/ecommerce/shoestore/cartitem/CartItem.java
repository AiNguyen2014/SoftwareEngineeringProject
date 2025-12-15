package ecommerce.shoestore.cartitem;

import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.shoes.Shoes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cartitem")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "shoeId", nullable = false)
    private Shoes shoes;

    private int quantity;

    private String description;

    protected CartItem(Cart cart, Shoes shoes, int quantity) {
        this.cart = cart;
        this.shoes = shoes;
        this.quantity = quantity;
    }

    public static CartItem create(Cart cart, Shoes shoes, int quantity) {
        return new CartItem(cart, shoes, quantity);
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }
}
