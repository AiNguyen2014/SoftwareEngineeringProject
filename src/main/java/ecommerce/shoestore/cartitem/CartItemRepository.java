package ecommerce.shoestore.cartitem;

import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndVariant(Cart cart, ShoesVariant variant);
}
